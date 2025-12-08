package payment.ProcessPayment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.Order;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;
import cgx.com.usecase.Payment.ProcessPayment.BankingPaymentStrategy;
import cgx.com.usecase.Payment.ProcessPayment.CodPaymentStrategy;
import cgx.com.usecase.Payment.ProcessPayment.IPaymentStrategy;
import cgx.com.usecase.Payment.ProcessPayment.PaymentResult;
import cgx.com.usecase.Payment.ProcessPayment.PaymentStrategyFactory;
import cgx.com.usecase.Payment.ProcessPayment.ProcessPaymentOutputBoundary;
import cgx.com.usecase.Payment.ProcessPayment.ProcessPaymentRequestData;
import cgx.com.usecase.Payment.ProcessPayment.ProcessPaymentResponseData;
import cgx.com.usecase.Payment.ProcessPayment.ProcessPaymentUseCase;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessPaymentUseCaseTest {

    // 1. Mock Dependencies
    @Mock private IOrderRepository mockOrderRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private PaymentStrategyFactory mockPaymentFactory;
    @Mock private ProcessPaymentOutputBoundary mockOutputBoundary;
    @Mock private IPaymentStrategy mockPaymentStrategy;

    // 2. Class under test
    private ProcessPaymentUseCase useCase;

    // 3. Test Data
    private AuthPrincipal customerPrincipal;
    private OrderData pendingOrderData;

    @BeforeEach
    void setUp() {
        useCase = new ProcessPaymentUseCase(mockOrderRepository, mockTokenValidator, mockPaymentFactory, mockOutputBoundary);

        // User giả lập
        customerPrincipal = new AuthPrincipal("user-1", "user@test.com", UserRole.CUSTOMER);

        // Đơn hàng giả lập trong DB (Mặc định là COD, PENDING)
        pendingOrderData = new OrderData();
        pendingOrderData.id = "order-123";
        pendingOrderData.userId = "user-1";
        pendingOrderData.shippingAddress = "Hanoi";
        pendingOrderData.status = "PENDING";
        pendingOrderData.paymentMethod = "COD"; 
        pendingOrderData.totalAmount = new BigDecimal("500000");
    }

    // =========================================================================
    // NHÓM 1: KỊCH BẢN THÀNH CÔNG (HAPPY PATHS)
    // =========================================================================

    @Test
    @DisplayName("KB1: Thanh toán COD (Giữ nguyên phương thức cũ)")
    void test_execute_COD_NoChange() {
        // GIVEN: User gửi request, không đổi phương thức (null hoặc giống cũ)
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "order-123", "COD");

        // Mock Behavior
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("order-123")).thenReturn(pendingOrderData);
        when(mockPaymentFactory.getStrategy("COD")).thenReturn(mockPaymentStrategy);
        
        // Mock Strategy trả về thành công
        PaymentResult result = new PaymentResult(true, "Tiền mặt", null, "COD-001");
        when(mockPaymentStrategy.process(any(Order.class))).thenReturn(result);

        // WHEN
        useCase.execute(input);

        // THEN
        ArgumentCaptor<ProcessPaymentResponseData> resCaptor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        ArgumentCaptor<OrderData> orderCaptor = ArgumentCaptor.forClass(OrderData.class);

        // 1. Verify DB được cập nhật trạng thái CONFIRMED (vì là COD)
        verify(mockOrderRepository).save(orderCaptor.capture());
        assertEquals("CONFIRMED", orderCaptor.getValue().status);
        assertEquals("COD", orderCaptor.getValue().paymentMethod);

        // 2. Verify Output
        verify(mockOutputBoundary).present(resCaptor.capture());
        assertTrue(resCaptor.getValue().success);
        assertEquals("Đặt hàng thành công. Vui lòng chuẩn bị tiền mặt.", resCaptor.getValue().message);
    }

    @Test
    @DisplayName("KB2: Đổi phương thức từ COD sang BANKING (Quan trọng)")
    void test_execute_SwitchToBanking() {
        // GIVEN: DB đang là COD (xem setUp), nhưng User chọn BANKING
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "order-123", "BANKING");

        // Mock Behavior
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("order-123")).thenReturn(pendingOrderData);
        
        // UseCase phải gọi Factory lấy strategy MỚI là BANKING
        when(mockPaymentFactory.getStrategy("BANKING")).thenReturn(mockPaymentStrategy);
        
        PaymentResult result = new PaymentResult(true, "Quét QR", "http://qr", "BANK-001");
        when(mockPaymentStrategy.process(any(Order.class))).thenReturn(result);

        // WHEN
        useCase.execute(input);

        // THEN
        ArgumentCaptor<OrderData> orderCaptor = ArgumentCaptor.forClass(OrderData.class);
        ArgumentCaptor<ProcessPaymentResponseData> resCaptor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);

        // 1. Verify DB được cập nhật phương thức mới là BANKING
        verify(mockOrderRepository).save(orderCaptor.capture());
        assertEquals("BANKING", orderCaptor.getValue().paymentMethod); // <-- Đã đổi thành công
        assertEquals("PENDING", orderCaptor.getValue().status); // Banking chưa xong nên vẫn PENDING

        // 2. Verify Output có Link
        verify(mockOutputBoundary).present(resCaptor.capture());
        assertTrue(resCaptor.getValue().success);
        assertEquals("http://qr", resCaptor.getValue().paymentUrl);
    }

    // =========================================================================
    // NHÓM 2: KỊCH BẢN NGOẠI LỆ (EXCEPTION PATHS)
    // =========================================================================

    @Test
    @DisplayName("Fail: Token rỗng")
    void test_fail_EmptyToken() {
        ProcessPaymentRequestData input = new ProcessPaymentRequestData(null, "order-123", null);
        useCase.execute(input);
        
        ArgumentCaptor<ProcessPaymentResponseData> captor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Auth Token không được để trống.", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Không tìm thấy đơn hàng")
    void test_fail_OrderNotFound() {
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "order-999", null);
        
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("order-999")).thenReturn(null);

        useCase.execute(input);

        ArgumentCaptor<ProcessPaymentResponseData> captor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        assertEquals("Không tìm thấy đơn hàng.", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Không phải chính chủ (Access Denied)")
    void test_fail_AccessDenied() {
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("hacker-token", "order-123", null);
        AuthPrincipal hacker = new AuthPrincipal("hacker", "h@h.com", UserRole.CUSTOMER);

        when(mockTokenValidator.validate("hacker-token")).thenReturn(hacker);
        when(mockOrderRepository.findById("order-123")).thenReturn(pendingOrderData);
        // pendingOrderData của "user-1", hacker là "hacker"

        useCase.execute(input);

        ArgumentCaptor<ProcessPaymentResponseData> captor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        assertEquals("Bạn không có quyền thanh toán cho đơn hàng này.", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Đơn hàng đã Hủy hoặc đã Xong (Sai trạng thái)")
    void test_fail_InvalidStatus() {
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "order-123", null);
        pendingOrderData.status = "CANCELLED";

        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("order-123")).thenReturn(pendingOrderData);

        useCase.execute(input);

        ArgumentCaptor<ProcessPaymentResponseData> captor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        assertEquals("Đơn hàng không ở trạng thái chờ thanh toán.", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Đổi sang phương thức không hợp lệ")
    void test_fail_InvalidNewMethod() {
        // User muốn đổi sang BITCOIN
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "order-123", "BITCOIN");

        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("order-123")).thenReturn(pendingOrderData);
        // PaymentMethod.valueOf("BITCOIN") sẽ ném lỗi

        useCase.execute(input);

        ArgumentCaptor<ProcessPaymentResponseData> captor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Phương thức thanh toán mới không hợp lệ.", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Lỗi hệ thống bất ngờ")
    void test_fail_SystemError() {
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "order-123", null);
        
        when(mockTokenValidator.validate("token")).thenThrow(new RuntimeException("DB Connection Timeout"));

        useCase.execute(input);

        ArgumentCaptor<ProcessPaymentResponseData> captor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        assertTrue(captor.getValue().message.contains("Lỗi hệ thống"));
    }
}