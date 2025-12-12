package Order.CancelOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Laptop;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderOutputBoundary;
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderRequestData;
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderResponseData;
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderUseCase;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelOrderUseCaseTest {

	@Mock private IOrderRepository orderRepository;
    @Mock private IDeviceRepository deviceRepository;
    @Mock private IUserRepository userRepository;
    @Mock private IAuthTokenValidator tokenValidator;
    @Mock private IDeviceMapper deviceMapper;
    @Mock private CancelOrderOutputBoundary outputBoundary;

    // Bỏ @InjectMocks để tránh nhập nhằng, ta sẽ new thủ công
    private CancelOrderUseCase cancelOrderUseCase;

    @BeforeEach
    void setUp() {
        // 1. Khởi tạo các Mock
        MockitoAnnotations.openMocks(this);

        cancelOrderUseCase = new CancelOrderUseCase(
            orderRepository,
            deviceRepository,
            userRepository,
            tokenValidator,
            deviceMapper,
            outputBoundary 
        );
    }

    @Test
    @DisplayName("Case: Thất bại khi Token không hợp lệ")
    void testExecute_InvalidToken_Fail() {
        // Arrange
        CancelOrderRequestData input = new CancelOrderRequestData();
        input.authToken = "invalid-token";
        input.orderId = "order-123";

        when(tokenValidator.validate(anyString())).thenThrow(new SecurityException("Token invalid"));

        // Act
        cancelOrderUseCase.execute(input);

        // Assert
        ArgumentCaptor<CancelOrderResponseData> captor = ArgumentCaptor.forClass(CancelOrderResponseData.class);
        verify(outputBoundary).present(captor.capture());
        CancelOrderResponseData response = captor.getValue();

        assertFalse(response.success);
        assertTrue(response.message.contains("Token invalid"));
    }

    @Test
    @DisplayName("Case: Thất bại khi Order ID bị rỗng")
    void testExecute_InvalidOrderId_Fail() {
        // Arrange
        CancelOrderRequestData input = new CancelOrderRequestData();
        input.authToken = "valid-token";
        input.orderId = ""; 

        when(tokenValidator.validate(anyString())).thenReturn(new AuthPrincipal("user-1", "admin@test.com", UserRole.CUSTOMER));

        // Act
        cancelOrderUseCase.execute(input);

        // Assert
        ArgumentCaptor<CancelOrderResponseData> captor = ArgumentCaptor.forClass(CancelOrderResponseData.class);
        verify(outputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("ID đơn hàng không được để trống.", captor.getValue().message);
    }

    @Test
    @DisplayName("Case: Thất bại khi không tìm thấy đơn hàng trong Database")
    void testExecute_OrderNotFound_Fail() {
        // Arrange
        CancelOrderRequestData input = new CancelOrderRequestData();
        input.authToken = "valid-token";
        input.orderId = "non-existent-id";

        when(tokenValidator.validate(anyString())).thenReturn(new AuthPrincipal("user-1", "test@test.com", UserRole.CUSTOMER));
        when(orderRepository.findById(input.orderId)).thenReturn(null); // Repo trả về null

        // Act
        cancelOrderUseCase.execute(input);

        // Assert
        ArgumentCaptor<CancelOrderResponseData> captor = ArgumentCaptor.forClass(CancelOrderResponseData.class);
        verify(outputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy đơn hàng.", captor.getValue().message);
    }

    @Test
    @DisplayName("Case: Thất bại khi người dùng không có quyền hủy đơn của người khác")
    void testExecute_UnauthorizedAccess_Fail() {
        // Arrange
        String orderOwnerId = "owner-user";
        String attackerId = "attacker-user";
        String orderId = "order-123";

        CancelOrderRequestData input = new CancelOrderRequestData();
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
        cancelOrderUseCase.execute(input);

        // Assert
        ArgumentCaptor<CancelOrderResponseData> captor = ArgumentCaptor.forClass(CancelOrderResponseData.class);
        verify(outputBoundary).present(captor.capture());
        CancelOrderResponseData response = captor.getValue();

        assertFalse(response.success);
        // Kiểm tra message từ User.validateAccess
        assertEquals("Bạn không có quyền xem hoặc thao tác trên đơn hàng này.", response.message);
    }

    @Test
    @DisplayName("Case: Thất bại khi cố gắng hủy đơn hàng đã SHIPPED")
    void testExecute_CancelShippedOrder_Fail() {
        // Arrange
        String userId = "user-1";
        String orderId = "order-shipped";

        CancelOrderRequestData input = new CancelOrderRequestData();
        input.authToken = "valid-token";
        input.orderId = orderId;

        // Mock Token hợp lệ
        when(tokenValidator.validate(anyString())).thenReturn(new AuthPrincipal(userId, "test@test.com", UserRole.CUSTOMER));

        // Mock Order đang ở trạng thái SHIPPED
        OrderData mockOrderData = new OrderData();
        mockOrderData.id = orderId;
        mockOrderData.userId = userId;
        mockOrderData.status = "SHIPPED"; // Quan trọng
        mockOrderData.paymentMethod = "COD";
        mockOrderData.totalAmount = BigDecimal.valueOf(100);
        when(orderRepository.findById(orderId)).thenReturn(mockOrderData);

        // Mock User
        UserData mockUserData = new UserData();
        mockUserData.userId = userId;
        mockUserData.role = UserRole.CUSTOMER;
        when(userRepository.findByUserId(userId)).thenReturn(mockUserData);

        // Act
        cancelOrderUseCase.execute(input);

        // Assert
        ArgumentCaptor<CancelOrderResponseData> captor = ArgumentCaptor.forClass(CancelOrderResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không thể hủy đơn hàng đã được xử lý hoặc đang giao.", captor.getValue().message);
    }

    @Test
    @DisplayName("Case 6: Thành công - Hủy đơn PENDING, cập nhật kho, lưu Order")
    void testExecute_Success() {
        // Arrange
        String userId = "user-1";
        String orderId = "order-success";
        String deviceId = "device-1";

        CancelOrderRequestData input = new CancelOrderRequestData();
        input.authToken = "valid-token";
        input.orderId = orderId;

        // 1. Mock Token
        when(tokenValidator.validate(anyString())).thenReturn(new AuthPrincipal(userId, "test@test.com", UserRole.CUSTOMER));

        // 2. Mock Order Data (Có 1 sản phẩm)
        OrderData mockOrderData = new OrderData();
        mockOrderData.id = orderId;
        mockOrderData.userId = userId;
        mockOrderData.status = "PENDING"; // Hợp lệ để hủy
        mockOrderData.paymentMethod = "COD";
        mockOrderData.totalAmount = BigDecimal.valueOf(1000);
        
        OrderItemData itemData = new OrderItemData();
        itemData.deviceId = deviceId;
        itemData.quantity = 2;
        itemData.unitPrice = BigDecimal.valueOf(500);
        mockOrderData.items = List.of(itemData);
        
        when(orderRepository.findById(orderId)).thenReturn(mockOrderData);

        // 3. Mock User
        UserData mockUserData = new UserData();
        mockUserData.userId = userId;
        mockUserData.role = UserRole.CUSTOMER;
        when(userRepository.findByUserId(userId)).thenReturn(mockUserData);

        // 4. Mock Device & Mapper (Để test logic hoàn kho)
        DeviceData mockDeviceData = new DeviceData();
        mockDeviceData.id = deviceId;
        mockDeviceData.stockQuantity = 10;
        when(deviceRepository.findById(deviceId)).thenReturn(mockDeviceData);
        
        ComputerDevice mockDeviceEntity = mock(ComputerDevice.class);
        when(deviceMapper.toEntity(mockDeviceData)).thenReturn(mockDeviceEntity);
        when(deviceMapper.toDTO(any())).thenReturn(mockDeviceData);

        // Act
        cancelOrderUseCase.execute(input);

        // Assert
        // 1. Verify hoàn kho: plusStock(2) phải được gọi
        verify(mockDeviceEntity).plusStock(2);
        verify(deviceRepository).save(any(DeviceData.class));

        // 2. Verify Order được lưu với trạng thái mới
        ArgumentCaptor<OrderData> orderCaptor = ArgumentCaptor.forClass(OrderData.class);
        verify(orderRepository).save(orderCaptor.capture());
        OrderData savedOrder = orderCaptor.getValue();
        assertEquals("CANCELLED", savedOrder.status);

        // 3. Verify Output boundary báo thành công
        ArgumentCaptor<CancelOrderResponseData> outputCaptor = ArgumentCaptor.forClass(CancelOrderResponseData.class);
        verify(outputBoundary).present(outputCaptor.capture());
        CancelOrderResponseData output = outputCaptor.getValue();
        
        assertTrue(output.success);
        assertEquals("Hủy đơn hàng thành công.", output.message);
        assertEquals("CANCELLED", output.status);
    }
}