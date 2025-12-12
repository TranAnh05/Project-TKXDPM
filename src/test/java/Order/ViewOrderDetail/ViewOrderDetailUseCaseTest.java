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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ViewOrderDetailUseCaseTest {

    @Mock
    private IOrderRepository orderRepository;
    @Mock
    private IAuthTokenValidator tokenValidator;
    @Mock
    private ViewOrderDetailOutputBoundary outputBoundary;

    private ViewOrderDetailUseCase useCase;

    // Dữ liệu mẫu
    private String userToken = "valid_token";
    private String userId = "user-123";
    private String orderId = "order-001";
    private ViewOrderDetailRequestData request;

    @BeforeEach
    void setUp() {
        useCase = new ViewOrderDetailUseCase(orderRepository, tokenValidator, outputBoundary);
        request = new ViewOrderDetailRequestData();
        request.authToken = userToken;
        request.orderId = orderId;
    }

    // Case: Token không hợp lệ
    @Test
    void testExecute_Fail_InvalidToken() {
        // Arrange
        when(tokenValidator.validate(anyString())).thenThrow(new SecurityException("Token không hợp lệ."));

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Token không hợp lệ.", captor.getValue().message);
    }

    // Case: ID đơn hàng không hợp lệ
    @Test
    void testExecute_Fail_InvalidOrderId() {
        // Arrange
        AuthPrincipal principal = new AuthPrincipal(userId, "test@mail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(userToken)).thenReturn(principal);
        
        request.orderId = ""; 

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("ID đơn hàng không được để trống"));
    }
    
    @Test
    void testExecute_Fail_OrderNotFound() {
        // Arrange
        AuthPrincipal principal = new AuthPrincipal(userId, "test@mail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(userToken)).thenReturn(principal);
        
        // Mock Repo trả về null
        when(orderRepository.findById(orderId)).thenReturn(null);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy đơn hàng.", captor.getValue().message);
    }

    // Case: Không phải chủ sở hữu của đơn hàng
    @Test
    void testExecute_Fail_AccessDenied() {
        // Arrange
        // 1. Người xem là Customer "user-123"
        AuthPrincipal principal = new AuthPrincipal(userId, "test@mail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(userToken)).thenReturn(principal);

        // 2. Đơn hàng thuộc về "other-user"
        OrderData orderData = new OrderData();
        orderData.id = orderId;
        orderData.userId = "other-user"; 
        orderData.status = "PENDING";
        orderData.paymentMethod = "COD";
        orderData.totalAmount = BigDecimal.valueOf(100);
        
        when(orderRepository.findById(orderId)).thenReturn(orderData);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Bạn không có quyền xem hoặc thao tác trên đơn hàng này.", captor.getValue().message);
    }

    // Case: Thành công
    @Test
    void testExecute_Success() {
        // Arrange
        // 1. Người xem là Owner ("user-123")
        AuthPrincipal principal = new AuthPrincipal(userId, "test@mail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(userToken)).thenReturn(principal);

        // 2. Đơn hàng thuộc về chính "user-123"
        OrderData orderData = new OrderData();
        orderData.id = orderId;
        orderData.userId = userId; // KHỚP với principal.userId
        orderData.status = "CONFIRMED";
        orderData.paymentMethod = "COD";
        orderData.totalAmount = BigDecimal.valueOf(500);
        orderData.items = new ArrayList<>(); 
        
        when(orderRepository.findById(orderId)).thenReturn(orderData);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        ViewOrderDetailResponseData response = captor.getValue();

        // Kiểm tra kết quả
        assertTrue(response.success);
        assertEquals("Lấy chi tiết đơn hàng thành công.", response.message);
        assertNotNull(response.order);
        assertEquals(orderId, response.order.id);
        assertEquals(userId, response.order.userId);
    }
}