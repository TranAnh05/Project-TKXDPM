package Order.ViewOrderDetail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderRequestData;
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderResponseData;
import cgx.com.usecase.ManageOrder.ViewOrderDetail.ViewOrderDetailOutputBoundary;
import cgx.com.usecase.ManageOrder.ViewOrderDetail.ViewOrderDetailRequestData;
import cgx.com.usecase.ManageOrder.ViewOrderDetail.ViewOrderDetailResponseData;
import cgx.com.usecase.ManageOrder.ViewOrderDetail.ViewOrderDetailUseCase;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ViewOrderDetailUseCaseTest {

    @Mock private IOrderRepository orderRepository;
    @Mock private IAuthTokenValidator tokenValidator;
    @Mock private IUserRepository userRepository;
    @Mock private ViewOrderDetailOutputBoundary outputBoundary;

    private ViewOrderDetailUseCase viewOrderDetailUseCase;

    @BeforeEach
    void setUp() {
        // 1. Khởi tạo Mock
        MockitoAnnotations.openMocks(this);

        viewOrderDetailUseCase = new ViewOrderDetailUseCase(
            orderRepository,
            tokenValidator,
            userRepository,
            outputBoundary
        );
    }

    @Test
    @DisplayName("Case: Thất bại khi Token không hợp lệ")
    void testExecute_InvalidToken_Fail() {
        // Arrange
        ViewOrderDetailRequestData input = new ViewOrderDetailRequestData();
        input.authToken = "invalid-token";
        input.orderId = "order-123";

        when(tokenValidator.validate(anyString()))
            .thenThrow(new SecurityException("Token hết hạn hoặc không hợp lệ"));

        // Act
        viewOrderDetailUseCase.execute(input);

        // Assert
        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        ViewOrderDetailResponseData response = captor.getValue();
        assertFalse(response.success);
        assertEquals("Token hết hạn hoặc không hợp lệ", response.message);
    }

    @Test
    @DisplayName("Case: Thất bại khi Order ID bị rỗng")
    void testExecute_InvalidOrderId_Fail() {
        // Arrange
        ViewOrderDetailRequestData input = new ViewOrderDetailRequestData();
        input.authToken = "valid-token";
        input.orderId = ""; // ID rỗng

        // Mock token hợp lệ để code chạy qua dòng đầu
        when(tokenValidator.validate(anyString())).thenReturn(new AuthPrincipal("user-1", "test@test.com", UserRole.CUSTOMER));

        // Act
        viewOrderDetailUseCase.execute(input);

        // Assert
        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("ID đơn hàng không được để trống.", captor.getValue().message);
    }

    @Test
    @DisplayName("Case: Thất bại khi không tìm thấy đơn hàng")
    void testExecute_OrderNotFound_Fail() {
        // Arrange
        ViewOrderDetailRequestData input = new ViewOrderDetailRequestData();
        input.authToken = "valid-token";
        input.orderId = "non-existent-order";

        when(tokenValidator.validate(anyString())).thenReturn(new AuthPrincipal("user-1", "test@test.com", UserRole.CUSTOMER));
        when(orderRepository.findById(input.orderId)).thenReturn(null); // Repo trả về null

        // Act
        viewOrderDetailUseCase.execute(input);

        // Assert
        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy đơn hàng.", captor.getValue().message);
    }

    @Test
    @DisplayName("Case: Không phải chủ sở hữu đơn hàng")
    void testExecute_UnauthorizedAccess_Fail() {
    	// Arrange
        String orderOwnerId = "owner-user";
        String attackerId = "attacker-user";
        String orderId = "order-123";

        ViewOrderDetailRequestData input = new ViewOrderDetailRequestData();
        input.authToken = "attacker-token";
        input.orderId = orderId;

        // Mock Token là của người khác (Role Customer)
        when(tokenValidator.validate(input.authToken)).thenReturn(new AuthPrincipal(attackerId, "attacker@test.com", UserRole.CUSTOMER));
        
        // Mock Order thuộc về owner-user
        OrderData mockOrderData = new OrderData();
        mockOrderData.id = orderId;
        mockOrderData.userId = orderOwnerId;
        mockOrderData.status = "PENDING";
        mockOrderData.paymentMethod = "COD"; 
        mockOrderData.totalAmount = BigDecimal.TEN;
        when(orderRepository.findById(orderId)).thenReturn(mockOrderData);

        // Mock User Owner
        UserData mockOwnerData = new UserData();
        mockOwnerData.userId = orderOwnerId;
        mockOwnerData.role = UserRole.CUSTOMER;
        when(userRepository.findByUserId(orderOwnerId)).thenReturn(mockOwnerData);

        // Act
        viewOrderDetailUseCase.execute(input);

        // Assert
        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        ViewOrderDetailResponseData response = captor.getValue();

        assertFalse(response.success);
        assertEquals("Bạn không có quyền xem hoặc thao tác trên đơn hàng này.", response.message);
    }
    
    @Test
    @DisplayName("Case: Thành công - Lấy chi tiết đơn hàng")
    void testExecute_Success() {
        // Arrange
        String userId = "user-1";
        String orderId = "order-success";
        
        ViewOrderDetailRequestData input = new ViewOrderDetailRequestData();
        input.authToken = "valid-token";
        input.orderId = orderId;

        // 1. Mock Token
        AuthPrincipal principal = new AuthPrincipal(userId, "test@test.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(anyString())).thenReturn(principal);

        // 2. Mock Order Data
        OrderData mockOrderData = new OrderData();
        mockOrderData.id = orderId;
        mockOrderData.userId = userId;
        mockOrderData.status = "PENDING";
        mockOrderData.totalAmount = BigDecimal.valueOf(500);
        
        when(orderRepository.findById(orderId)).thenReturn(mockOrderData);

        // 3. Mock User Data (Để validateAccess không bị NullPointerException khi mapToEntity)
        UserData mockUserData = new UserData();
        mockUserData.userId = userId;
        mockUserData.email = "test@test.com";
        mockUserData.role = UserRole.CUSTOMER;
        mockUserData.status = AccountStatus.ACTIVE;
        mockUserData.createdAt = Instant.now();
        mockUserData.updatedAt = Instant.now();
        
        when(userRepository.findByUserId(userId)).thenReturn(mockUserData);

        // Act
        viewOrderDetailUseCase.execute(input);

        // Assert
        ArgumentCaptor<ViewOrderDetailResponseData> captor = ArgumentCaptor.forClass(ViewOrderDetailResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        ViewOrderDetailResponseData response = captor.getValue();
        
        assertTrue(response.success);
        assertEquals("Lấy chi tiết đơn hàng thành công.", response.message);
        assertNotNull(response.order);
        assertEquals(orderId, response.order.id);
        assertEquals(BigDecimal.valueOf(500), response.order.totalAmount);
    }
}