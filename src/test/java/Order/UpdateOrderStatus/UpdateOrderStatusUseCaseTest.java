package Order.UpdateOrderStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.Laptop;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageOrder.UpdateOrderStatus.UpdateOrderStatusOutputBoundary;
import cgx.com.usecase.ManageOrder.UpdateOrderStatus.UpdateOrderStatusRequestData;
import cgx.com.usecase.ManageOrder.UpdateOrderStatus.UpdateOrderStatusResponseData;
import cgx.com.usecase.ManageOrder.UpdateOrderStatus.UpdateOrderStatusUseCase;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateOrderStatusUseCaseTest {

    @Mock private IOrderRepository orderRepository;
    @Mock private IDeviceRepository deviceRepository;
    @Mock private IAuthTokenValidator tokenValidator;
    @Mock private IDeviceMapper deviceMapper;
    @Mock private UpdateOrderStatusOutputBoundary outputBoundary;

    private UpdateOrderStatusUseCase useCase;

    // Dữ liệu mẫu
    private String adminToken = "admin_token";
    private String orderId = "order-001";
    private UpdateOrderStatusRequestData request;

    @BeforeEach
    void setUp() {
        useCase = new UpdateOrderStatusUseCase(orderRepository, deviceRepository, tokenValidator, deviceMapper, outputBoundary);
        request = new UpdateOrderStatusRequestData(adminToken, orderId, "CONFIRMED");
    }

    private void mockAdminAuth() {
        AuthPrincipal admin = new AuthPrincipal("admin-id", "admin@test.com", UserRole.ADMIN);
        when(tokenValidator.validate(adminToken)).thenReturn(admin);
    }

    // Case: không phải admin
    @Test
    void testExecute_Fail_NotAdmin() {
        AuthPrincipal customer = new AuthPrincipal("cust-id", "c@test.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(anyString())).thenReturn(customer);

        useCase.execute(request);

        ArgumentCaptor<UpdateOrderStatusResponseData> captor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        verify(outputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không có quyền truy cập.", captor.getValue().message);
    }

    // Case: Trạng thái mới không hợp lệ
    @Test
    void testExecute_Fail_InvalidStatusEnum() {
        mockAdminAuth();
        UpdateOrderStatusRequestData invalidReq = new UpdateOrderStatusRequestData(adminToken, orderId, "INVALID_STATUS");

        useCase.execute(invalidReq);

        ArgumentCaptor<UpdateOrderStatusResponseData> captor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Message từ Order.convertToOrderStatus
        assertTrue(captor.getValue().message.contains("Trạng thái đơn hàng không hợp lệ"));
    }
    
    // Case: ID không hợp lệ
    @Test
    void testExecute_Fail_InvalidID() {
        mockAdminAuth();
        UpdateOrderStatusRequestData invalidReq = new UpdateOrderStatusRequestData(adminToken, "", "INVALID_STATUS");

        useCase.execute(invalidReq);

        ArgumentCaptor<UpdateOrderStatusResponseData> captor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Message từ Order.convertToOrderStatus
        assertTrue(captor.getValue().message.contains("ID đơn hàng không được để trống."));
    }

    // Case: Không tìm thấy đơn hàng
    @Test
    void testExecute_Fail_OrderNotFound() {
        mockAdminAuth();
        when(orderRepository.findById(orderId)).thenReturn(null);

        useCase.execute(request);

        ArgumentCaptor<UpdateOrderStatusResponseData> captor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy đơn hàng.", captor.getValue().message);
    }
    
    // Case: Hủy đơn đang giao
    @Test
    void testExecute_Fail_InvalidTransition01() {
        // Arrange
        mockAdminAuth();
        // Request muốn HỦY đơn
        UpdateOrderStatusRequestData cancelReq = new UpdateOrderStatusRequestData(adminToken, orderId, "CANCELLED");

        OrderData orderData = new OrderData();
        orderData.id = orderId;
        orderData.status = "SHIPPED"; // Đang giao
        orderData.paymentMethod = "COD";
        when(orderRepository.findById(orderId)).thenReturn(orderData);

        // Act
        useCase.execute(cancelReq);

        // Assert
        ArgumentCaptor<UpdateOrderStatusResponseData> captor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Message từ Order.updateStatus check logic
        assertEquals("Không thể hủy đơn hàng đã được xử lý hoặc đang giao.", captor.getValue().message);
    }
    
    // Case: Hủy đơn đã giao
    @Test
    void testExecute_Fail_InvalidTransition() {
        // Arrange
        mockAdminAuth();
        // Request muốn HỦY đơn
        UpdateOrderStatusRequestData cancelReq = new UpdateOrderStatusRequestData(adminToken, orderId, "CANCELLED");

        // Mock Order đang ở trạng thái DELIVERED (Đã giao)
        OrderData orderData = new OrderData();
        orderData.id = orderId;
        orderData.status = "DELIVERED"; // Đã giao xong
        orderData.paymentMethod = "COD";
        when(orderRepository.findById(orderId)).thenReturn(orderData);

        // Act
        useCase.execute(cancelReq);

        // Assert
        ArgumentCaptor<UpdateOrderStatusResponseData> captor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Message từ Order.updateStatus check logic
        assertEquals("Không thể hủy đơn hàng đã được xử lý hoặc đang giao.", captor.getValue().message);
    }

    // Case: Thành công - không hoàn kho
    @Test
    void testExecute_Success_NormalUpdate() {
        // Arrange
        mockAdminAuth();
        UpdateOrderStatusRequestData shipReq = new UpdateOrderStatusRequestData(adminToken, orderId, "SHIPPED");

        OrderData orderData = new OrderData();
        orderData.id = orderId;
        orderData.status = "PENDING";
        orderData.paymentMethod = "COD";
        orderData.totalAmount = BigDecimal.valueOf(100);
        when(orderRepository.findById(orderId)).thenReturn(orderData);

        // Act
        useCase.execute(shipReq);

        // Assert
        ArgumentCaptor<UpdateOrderStatusResponseData> captor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertTrue(captor.getValue().success);
        assertEquals("SHIPPED", captor.getValue().status);
        
        // Kiểm tra Order được lưu
        verify(orderRepository).save(any(OrderData.class));
        // Kiểm tra KHÔNG gọi hoàn kho (Device Repo không được gọi save)
        verify(deviceRepository, never()).save(any());
    }

    // Case: thành công - hoàn kho
    @Test
    void testExecute_Success_CancelWithRestock() {
        // Arrange
        mockAdminAuth();
        UpdateOrderStatusRequestData cancelReq = new UpdateOrderStatusRequestData(adminToken, orderId, "CANCELLED");

        // Mock Order có 1 sản phẩm (Laptop, số lượng mua: 2)
        OrderData orderData = new OrderData();
        orderData.id = orderId;
        orderData.status = "CONFIRMED"; 
        orderData.paymentMethod = "COD";
        orderData.totalAmount = BigDecimal.valueOf(2000);
        
        List<OrderItemData> items = new ArrayList<>();
        items.add(new OrderItemData("laptop-1", "Dell", "img", BigDecimal.valueOf(1000), 2)); // Mua 2 cái
        orderData.items = items;

        when(orderRepository.findById(orderId)).thenReturn(orderData);

        // Mock Device Data (Tồn kho hiện tại: 5)
        DeviceData laptopDTO = new DeviceData();
        laptopDTO.id = "laptop-1";
        laptopDTO.stockQuantity = 5;
        when(deviceRepository.findById("laptop-1")).thenReturn(laptopDTO);

        // Mock Mapper: Chuyển DTO thành Entity thật để tính toán plusStock
        // Lưu ý: Dùng Mock hay Real Object đều được, ở đây dùng Real Object để test logic cộng trừ
        Laptop laptopEntity = new Laptop("laptop-1", "Dell", "desc", BigDecimal.valueOf(1000), 5, "cat", "NEW", "img", null, null, "cpu", "ram", "ssd", 14.0);
        when(deviceMapper.toEntity(laptopDTO)).thenReturn(laptopEntity);

        // Mock Mapper ngược lại: Entity -> DTO (để save)
        DeviceData updatedDTO = new DeviceData();
        updatedDTO.id = "laptop-1";
        updatedDTO.stockQuantity = 7; // 5 + 2 = 7
        when(deviceMapper.toDTO(laptopEntity)).thenReturn(updatedDTO);

        // Act
        useCase.execute(cancelReq);

        // Assert
        ArgumentCaptor<UpdateOrderStatusResponseData> responseCaptor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        verify(outputBoundary).present(responseCaptor.capture());
        
        // 1. Kiểm tra Output
        assertTrue(responseCaptor.getValue().success);
        assertEquals("CANCELLED", responseCaptor.getValue().status);

        // 2. Kiểm tra Hoàn kho (Quan trọng nhất)
        ArgumentCaptor<DeviceData> deviceCaptor = ArgumentCaptor.forClass(DeviceData.class);
        verify(deviceRepository).save(deviceCaptor.capture());
        
        // Verify rằng kho đã được cộng thêm 2 (5 ban đầu + 2 trả lại = 7)
        assertEquals(7, deviceCaptor.getValue().stockQuantity);

        // 3. Kiểm tra Lưu Order
        verify(orderRepository).save(any(OrderData.class));
    }
}