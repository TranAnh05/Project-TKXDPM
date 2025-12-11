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
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.Payment.BankingPaymentStrategy;
import cgx.com.usecase.Payment.CodPaymentStrategy;
import cgx.com.usecase.Payment.IPaymentStrategy;
import cgx.com.usecase.Payment.PaymentResult;
import cgx.com.usecase.Payment.PaymentStrategyFactory;
import cgx.com.usecase.Payment.ProcessPayment.ProcessPaymentOutputBoundary;
import cgx.com.usecase.Payment.ProcessPayment.ProcessPaymentRequestData;
import cgx.com.usecase.Payment.ProcessPayment.ProcessPaymentResponseData;
import cgx.com.usecase.Payment.ProcessPayment.ProcessPaymentUseCase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessPaymentUseCaseTest {

    // 1. Mock Dependencies
    @Mock private IOrderRepository mockOrderRepo;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private PaymentStrategyFactory mockPaymentFactory;
    @Mock private ProcessPaymentOutputBoundary mockOutputBoundary;
    @Mock private IPaymentStrategy mockStrategy; // Mock chiến lược thanh toán

    // 2. Class under test
    private ProcessPaymentUseCase useCase;

    // 3. Test Data
    private AuthPrincipal userPrincipal;
    private OrderData existingOrderData;

    @BeforeEach
    void setUp() {
        useCase = new ProcessPaymentUseCase(mockOrderRepo, mockTokenValidator, mockPaymentFactory, mockOutputBoundary);
        
        // Mock User
        userPrincipal = new AuthPrincipal("user-1", "test@mail.com", UserRole.CUSTOMER);
        
        // Mock Order Data trong DB (Trạng thái mặc định PENDING, Payment BANKING)
        existingOrderData = new OrderData();
        existingOrderData.id = "order-123";
        existingOrderData.userId = "user-1";
        existingOrderData.status = "PENDING";
        existingOrderData.paymentMethod = "BANKING";
        existingOrderData.totalAmount = new BigDecimal("500000");
        existingOrderData.shippingAddress = "Ha Noi";
        existingOrderData.createdAt = Instant.now();
        existingOrderData.updatedAt = Instant.now();
    }
    @Test
    @DisplayName("Fail: Token không hợp lệ")
    void test_Fail_InvalidToken() {
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("invalid-token", "order-123", "COD");
        
        when(mockTokenValidator.validate("invalid-token")).thenThrow(new SecurityException("Token lỗi"));

        useCase.execute(input);

        ArgumentCaptor<ProcessPaymentResponseData> captor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Token lỗi", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Order ID bị rỗng")
    void test_Fail_InvalidOrderId() {
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "", "COD"); // ID rỗng
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        
        useCase.execute(input);

        ArgumentCaptor<ProcessPaymentResponseData> captor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Lỗi từ Order.validateId
        assertTrue(captor.getValue().message.contains("ID đơn hàng không được để trống"));
    }
    
    @Test
    @DisplayName("Fail: Phương thức thanh toán rỗng")
    void test_Fail_EmptyPaymentMethod() {
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "order-123", ""); // Rỗng
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        
        useCase.execute(input);

        ArgumentCaptor<ProcessPaymentResponseData> captor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Lỗi từ Order.validatePaymentStatus
        assertTrue(captor.getValue().message.contains("không được để trống"));
    }

    @Test
    @DisplayName("Fail: Đơn hàng đã Hủy hoặc Đã giao (Không phải PENDING/CONFIRMED)")
    void test_Fail_InvalidOrderStatus() {
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "order-123", "COD");
        existingOrderData.status = "CANCELLED"; // Đã hủy
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockOrderRepo.findById("order-123")).thenReturn(existingOrderData);

        useCase.execute(input);

        ArgumentCaptor<ProcessPaymentResponseData> captor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Đơn hàng không ở trạng thái chờ thanh toán.", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Gửi lên phương thức lạ (VD: BITCOIN)")
    void test_Fail_InvalidPaymentMethodEnum() {
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "order-123", "BITCOIN");
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);

        useCase.execute(input);

        ArgumentCaptor<ProcessPaymentResponseData> captor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Lỗi từ Order.convertToPaymentMethod
        assertTrue(captor.getValue().message.contains("không hợp lệ"));
    }

    @Test
    @DisplayName("Success: Chọn COD -> Đổi trạng thái CONFIRMED -> Save")
    void test_Success_COD() {
        // GIVEN: Đơn cũ là Banking, User đổi sang COD
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "order-123", "COD");
        existingOrderData.paymentMethod = "BANKING"; // Cũ
        
        PaymentResult successResult = new PaymentResult(true, "Vui lòng chuẩn bị tiền mặt", null, null);

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockOrderRepo.findById("order-123")).thenReturn(existingOrderData);
        when(mockPaymentFactory.getStrategy("COD")).thenReturn(mockStrategy);
        when(mockStrategy.process(any(Order.class))).thenReturn(successResult);

        // WHEN
        useCase.execute(input);

        // THEN
        // 1. Verify Save: Trạng thái phải là CONFIRMED, Method là COD
        ArgumentCaptor<OrderData> orderCaptor = ArgumentCaptor.forClass(OrderData.class);
        verify(mockOrderRepo).save(orderCaptor.capture());
        OrderData savedOrder = orderCaptor.getValue();
        
        assertEquals("CONFIRMED", savedOrder.status);
        assertEquals("COD", savedOrder.paymentMethod);

        // 2. Verify Output
        ArgumentCaptor<ProcessPaymentResponseData> resCaptor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        
        assertTrue(resCaptor.getValue().success);
        assertEquals(null, resCaptor.getValue().transactionRef);
        assertTrue(resCaptor.getValue().message.contains("tiền mặt"));
    }

    @Test
    @DisplayName("Success: Chọn BANKING -> Giữ PENDING -> Trả về URL")
    void test_Success_Banking() {
        // GIVEN: Đơn cũ là COD, User đổi sang BANKING
        ProcessPaymentRequestData input = new ProcessPaymentRequestData("token", "order-123", "BANKING");
        existingOrderData.paymentMethod = "COD"; // Cũ
        
        PaymentResult successResult = new PaymentResult(true, "Vui lòng quét mã để thanh toán", "https://bank.com/pay", "trans-002");

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockOrderRepo.findById("order-123")).thenReturn(existingOrderData);
        when(mockPaymentFactory.getStrategy("BANKING")).thenReturn(mockStrategy);
        when(mockStrategy.process(any(Order.class))).thenReturn(successResult);

        // WHEN
        useCase.execute(input);

        // THEN
        // 1. Verify Save: Trạng thái vẫn là PENDING (chờ tiền về), Method là BANKING
        ArgumentCaptor<OrderData> orderCaptor = ArgumentCaptor.forClass(OrderData.class);
        verify(mockOrderRepo).save(orderCaptor.capture());
        OrderData savedOrder = orderCaptor.getValue();
        
        assertEquals("PENDING", savedOrder.status); // Logic code: else { ... } không update status
        assertEquals("BANKING", savedOrder.paymentMethod);

        // 2. Verify Output
        ArgumentCaptor<ProcessPaymentResponseData> resCaptor = ArgumentCaptor.forClass(ProcessPaymentResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        
        assertTrue(resCaptor.getValue().success);
        assertEquals("https://bank.com/pay", resCaptor.getValue().paymentUrl);
        assertTrue(resCaptor.getValue().message.contains("qua ngân hàng"));
    }
}