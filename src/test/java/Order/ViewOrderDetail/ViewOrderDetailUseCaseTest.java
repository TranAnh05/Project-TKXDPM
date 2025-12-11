package Order.ViewOrderDetail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageOrder.ViewOrderDetail.ViewOrderDetailOutputBoundary;
import cgx.com.usecase.ManageOrder.ViewOrderDetail.ViewOrderDetailRequestData;
import cgx.com.usecase.ManageOrder.ViewOrderDetail.ViewOrderDetailResponseData;
import cgx.com.usecase.ManageOrder.ViewOrderDetail.ViewOrderDetailUseCase;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ViewOrderDetailUseCaseTest {

    @Mock private IOrderRepository mockOrderRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private ViewOrderDetailOutputBoundary mockOutputBoundary;

    private ViewOrderDetailUseCase useCase;
    private AuthPrincipal customerPrincipal;
    private OrderData myOrder;
    private OrderData otherOrder;

    @BeforeEach
    void setUp() {
        useCase = new ViewOrderDetailUseCase(mockOrderRepository, mockTokenValidator, mockOutputBoundary);
        customerPrincipal = new AuthPrincipal("user-1", "user@test.com", UserRole.CUSTOMER);
        
        // Đơn của mình
        myOrder = new OrderData();
        myOrder.id = "ord-1"; 
        myOrder.userId = "user-1"; 
        myOrder.items = List.of(new OrderItemData("d1", "Lap", "img", BigDecimal.TEN, 1));
        
        // Đơn của người khác
        otherOrder = new OrderData();
        otherOrder.id = "ord-2"; 
        otherOrder.userId = "user-2"; // Khác user-1
    }

    // Case 1: Thành công - Xem đơn của chính mình
    @Test
    void test_execute_success_ownOrder() {
        ViewOrderDetailRequestData input = new ViewOrderDetailRequestData("token", "ord-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("ord-1")).thenReturn(myOrder);

        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);

        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        ViewOrderDetailResponseData response = captor.getValue();

        assertTrue(response.success);
        assertEquals("ord-1", response.order.id);
        assertEquals(1, response.order.items.size());
    }

    // Case 2: Lỗi - Xem đơn của người khác (Security)
    @Test
    void test_execute_failure_otherUserOrder() {
        ViewOrderDetailRequestData input = new ViewOrderDetailRequestData("token", "ord-2");
        
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("ord-2")).thenReturn(otherOrder);

        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);

        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Bạn không có quyền xem đơn hàng này.", captor.getValue().message);
    }
    
    // Case 3: Thành công - Admin xem đơn của bất kỳ ai
    @Test
    void test_execute_success_adminViewOther() {
        ViewOrderDetailRequestData input = new ViewOrderDetailRequestData("admin-token", "ord-2");
        AuthPrincipal admin = new AuthPrincipal("admin", "e", UserRole.ADMIN);
        
        when(mockTokenValidator.validate("admin-token")).thenReturn(admin);
        when(mockOrderRepository.findById("ord-2")).thenReturn(otherOrder);

        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);

        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertTrue(captor.getValue().success); // Admin được phép
        assertEquals("ord-2", captor.getValue().order.id);
    }

    // Case 4: Lỗi - Không tìm thấy đơn
    @Test
    void test_execute_failure_notFound() {
        ViewOrderDetailRequestData input = new ViewOrderDetailRequestData("token", "ord-999");
        
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("ord-999")).thenReturn(null);

        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);

        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy đơn hàng.", captor.getValue().message);
    }
    
    // Case 5: Lỗi - Input rỗng
    @Test
    void test_execute_failure_emptyId() {
        ViewOrderDetailRequestData input = new ViewOrderDetailRequestData("token", "");
        
        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("ID đơn hàng không được để trống.", captor.getValue().message);
    }

    // --- CASE 6 (MỚI): Lỗi Hệ thống (Database Crash) ---
    @Test
    void test_execute_failure_dbCrash() {
        // ARRANGE
        ViewOrderDetailRequestData input = new ViewOrderDetailRequestData("token", "ord-1");
        
        // Token OK
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        
        // Database ném Exception khi gọi findById
        doThrow(new RuntimeException("Connection refused")).when(mockOrderRepository).findById("ord-1");

        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockOutputBoundary).present(captor.capture());
        ViewOrderDetailResponseData response = captor.getValue();

        assertFalse(response.success);
        assertTrue(response.message.contains("Lỗi hệ thống"), "Message thực tế: " + response.message);
    }
}