package Order.UpdateOrderStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.Laptop;
import cgx.com.Entities.UserRole;
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
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateOrderStatusUseCaseTest {

    @Mock private IOrderRepository mockOrderRepository;
    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IDeviceMapper mockDeviceMapper;
    @Mock private UpdateOrderStatusOutputBoundary mockOutputBoundary;

    private UpdateOrderStatusUseCase useCase;
    private AuthPrincipal adminPrincipal;
    private OrderData pendingOrder;
    private OrderData shippedOrder;
    private DeviceData deviceData;
    private Laptop laptopEntity;

    @BeforeEach
    void setUp() {
    	useCase = new UpdateOrderStatusUseCase(
                mockOrderRepository, mockDeviceRepository, mockTokenValidator, mockDeviceMapper, mockOutputBoundary
            );
            
adminPrincipal = new AuthPrincipal("admin", "e", UserRole.ADMIN);
        
        // Đơn PENDING
        pendingOrder = new OrderData();
        pendingOrder.id = "ord-1"; 
        pendingOrder.userId = "u1"; 
        pendingOrder.status = "PENDING";
        pendingOrder.shippingAddress = "123 Street"; // Cần có địa chỉ
        pendingOrder.items = List.of(new OrderItemData("dev-1", "Lap", "img", BigDecimal.TEN, 1));

        // Đơn SHIPPED (FIX: Bổ sung dữ liệu để tránh lỗi Validation)
        shippedOrder = new OrderData();
        shippedOrder.id = "ord-2"; 
        shippedOrder.userId = "u1";             // <-- Thêm dòng này
        shippedOrder.shippingAddress = "123 Street"; // <-- Thêm dòng này
        shippedOrder.status = "SHIPPED";

        // Sản phẩm
        deviceData = new DeviceData(); deviceData.id = "dev-1"; deviceData.stockQuantity = 10;
        laptopEntity = new Laptop("dev-1", "Lap", "D", BigDecimal.TEN, 10, "c", "ACTIVE", "i", Instant.now(), Instant.now(), "i7", "8G", "S", 14.0);
    }

    // Case 1: Thành công - Chuyển PENDING -> CONFIRMED
    @Test
    void test_execute_success_confirm() {
        UpdateOrderStatusRequestData input = new UpdateOrderStatusRequestData("token", "ord-1", "CONFIRMED");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockOrderRepository.findById("ord-1")).thenReturn(pendingOrder);

        ArgumentCaptor<OrderData> orderCaptor = ArgumentCaptor.forClass(OrderData.class);
        ArgumentCaptor<UpdateOrderStatusResponseData> responseCaptor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);

        useCase.execute(input);

        verify(mockOrderRepository).save(orderCaptor.capture());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertEquals("CONFIRMED", orderCaptor.getValue().status);
        assertTrue(responseCaptor.getValue().success);
        
        // Không được gọi hoàn kho
        verify(mockDeviceRepository, never()).save(any());
    }

    // Case 2: Thành công - Admin Hủy đơn (CANCELLED) -> Hoàn kho
    @Test
    void test_execute_success_cancel_restock() {
        UpdateOrderStatusRequestData input = new UpdateOrderStatusRequestData("token", "ord-1", "CANCELLED");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockOrderRepository.findById("ord-1")).thenReturn(pendingOrder);
        // Setup hoàn kho
        when(mockDeviceRepository.findById("dev-1")).thenReturn(deviceData);
        when(mockDeviceMapper.toEntity(deviceData)).thenReturn(laptopEntity);
        when(mockDeviceMapper.toDTO(any())).thenReturn(deviceData);

        useCase.execute(input);

        // Verify hoàn kho
        verify(mockDeviceRepository).save(any());
        assertEquals(11, laptopEntity.getStockQuantity()); // 10 + 1 = 11
    }

    // Case 3: Lỗi - Không phải Admin
    @Test
    void test_execute_failure_notAdmin() {
        UpdateOrderStatusRequestData input = new UpdateOrderStatusRequestData("token", "ord-1", "CONFIRMED");
        AuthPrincipal customer = new AuthPrincipal("cust", "e", UserRole.CUSTOMER);
        when(mockTokenValidator.validate("token")).thenReturn(customer);

        ArgumentCaptor<UpdateOrderStatusResponseData> captor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không có quyền truy cập (Yêu cầu Admin).", captor.getValue().message);
    }

    // Case 4: Lỗi Logic - Hủy đơn đã SHIPPED
    @Test
    void test_execute_failure_cancelShipped() {
        UpdateOrderStatusRequestData input = new UpdateOrderStatusRequestData("token", "ord-2", "CANCELLED");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockOrderRepository.findById("ord-2")).thenReturn(shippedOrder);

        ArgumentCaptor<UpdateOrderStatusResponseData> captor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        
        // Debug: In ra message lỗi thực tế nếu test fail
        if (!captor.getValue().message.contains("Không thể hủy")) {
            System.out.println("Message thực tế: " + captor.getValue().message);
        }

        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Không thể hủy đơn hàng"));
    }

    // Case 5: Lỗi Input - Status không hợp lệ
    @Test
    void test_execute_failure_invalidStatus() {
        UpdateOrderStatusRequestData input = new UpdateOrderStatusRequestData("token", "ord-1", "INVALID_STATUS");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        
        ArgumentCaptor<UpdateOrderStatusResponseData> captor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Trạng thái không hợp lệ"));
    }
    
    // Case 6: DB Crash
    @Test
    void test_execute_failure_dbCrash() {
        UpdateOrderStatusRequestData input = new UpdateOrderStatusRequestData("token", "ord-1", "CONFIRMED");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockOrderRepository.findById("ord-1")).thenReturn(pendingOrder);
        
        doThrow(new RuntimeException("DB Error")).when(mockOrderRepository).save(any());

        ArgumentCaptor<UpdateOrderStatusResponseData> captor = ArgumentCaptor.forClass(UpdateOrderStatusResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Lỗi hệ thống"));
    }
}