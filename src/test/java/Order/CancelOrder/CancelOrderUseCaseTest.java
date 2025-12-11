package Order.CancelOrder;

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
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderOutputBoundary;
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderRequestData;
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderResponseData;
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderUseCase;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CancelOrderUseCaseTest {

    @Mock private IOrderRepository mockOrderRepository;
    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IDeviceMapper mockDeviceMapper;
    @Mock private CancelOrderOutputBoundary mockOutputBoundary;

    private CancelOrderUseCase useCase;
    private AuthPrincipal customerPrincipal;
    private OrderData pendingOrder;
    private OrderData shippedOrder;
    private DeviceData deviceData;
    private Laptop laptopEntity;

    @BeforeEach
    void setUp() {
        useCase = new CancelOrderUseCase(
            mockOrderRepository, mockDeviceRepository, mockTokenValidator, mockDeviceMapper, mockOutputBoundary
        );
        
        customerPrincipal = new AuthPrincipal("user-1", "e", UserRole.CUSTOMER);
        
        // Đơn hàng PENDING (Có thể hủy)
        pendingOrder = new OrderData();
        pendingOrder.id = "ord-1"; pendingOrder.userId = "user-1"; pendingOrder.status = "PENDING";
        pendingOrder.items = List.of(new OrderItemData("dev-1", "Lap", "img", BigDecimal.TEN, 2)); // Mua 2 cái

        // Đơn hàng SHIPPED (Không thể hủy)
        shippedOrder = new OrderData();
        shippedOrder.id = "ord-2"; shippedOrder.userId = "user-1"; shippedOrder.status = "SHIPPED";

        // Sản phẩm
        deviceData = new DeviceData(); deviceData.id = "dev-1"; deviceData.stockQuantity = 10; deviceData.cpu = "i7";
        
        laptopEntity = new Laptop("dev-1", "Lap", "D", BigDecimal.TEN, 10, "c", "ACTIVE", "i", Instant.now(), Instant.now(), "i7", "8G", "S", 14.0);
    }

    // Case 1: Thành công - Hủy đơn & Hoàn kho
    @Test
    void test_execute_success() {
        CancelOrderRequestData input = new CancelOrderRequestData("token", "ord-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("ord-1")).thenReturn(pendingOrder);
        when(mockDeviceRepository.findById("dev-1")).thenReturn(deviceData);
        when(mockDeviceMapper.toEntity(deviceData)).thenReturn(laptopEntity);
        when(mockDeviceMapper.toDTO(any())).thenReturn(deviceData);

        ArgumentCaptor<OrderData> orderCaptor = ArgumentCaptor.forClass(OrderData.class);
        ArgumentCaptor<DeviceData> deviceCaptor = ArgumentCaptor.forClass(DeviceData.class);
        ArgumentCaptor<CancelOrderResponseData> responseCaptor = ArgumentCaptor.forClass(CancelOrderResponseData.class);

        useCase.execute(input);

        // Verify save Order (update status)
        verify(mockOrderRepository).save(orderCaptor.capture());
        assertEquals("CANCELLED", orderCaptor.getValue().status);

        // Verify save Device (restock)
        verify(mockDeviceRepository).save(deviceCaptor.capture());
        // Tồn kho ban đầu 10 + Trả lại 2 = 12
        assertEquals(12, laptopEntity.getStockQuantity());

        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertTrue(responseCaptor.getValue().success);
    }

    // Case 2: Lỗi - Đơn hàng đã SHIPPED
    @Test
    void test_execute_failure_shipped() {
        CancelOrderRequestData input = new CancelOrderRequestData("token", "ord-2");
        
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("ord-2")).thenReturn(shippedOrder);

        ArgumentCaptor<CancelOrderResponseData> captor = ArgumentCaptor.forClass(CancelOrderResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không thể hủy đơn hàng đã được xử lý hoặc đang giao.", captor.getValue().message);
        
        verify(mockDeviceRepository, never()).save(any()); // Không được hoàn kho
    }

    // Case 3: Lỗi - Không phải chủ đơn (Security)
    @Test
    void test_execute_failure_notOwner() {
        CancelOrderRequestData input = new CancelOrderRequestData("token", "ord-1");
        AuthPrincipal otherUser = new AuthPrincipal("user-2", "e", UserRole.CUSTOMER);
        
        when(mockTokenValidator.validate("token")).thenReturn(otherUser);
        when(mockOrderRepository.findById("ord-1")).thenReturn(pendingOrder); // Chủ là user-1

        ArgumentCaptor<CancelOrderResponseData> captor = ArgumentCaptor.forClass(CancelOrderResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Bạn không có quyền hủy đơn hàng này.", captor.getValue().message);
    }

    // Case 4: Lỗi - Không tìm thấy đơn
    @Test
    void test_execute_failure_notFound() {
        CancelOrderRequestData input = new CancelOrderRequestData("token", "ord-999");
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("ord-999")).thenReturn(null);

        ArgumentCaptor<CancelOrderResponseData> captor = ArgumentCaptor.forClass(CancelOrderResponseData.class);
        useCase.execute(input);

        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy đơn hàng.", captor.getValue().message);
    }

    // --- CASE 5 (MỚI): Lỗi Hệ Thống (Database Crash) ---
    @Test
    void test_execute_failure_dbCrash() {
        // ARRANGE
        CancelOrderRequestData input = new CancelOrderRequestData("token", "ord-1");
        
        // Mọi thứ hợp lệ
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockOrderRepository.findById("ord-1")).thenReturn(pendingOrder);
        when(mockDeviceRepository.findById("dev-1")).thenReturn(deviceData);
        when(mockDeviceMapper.toEntity(deviceData)).thenReturn(laptopEntity);
        
        // GIẢ LẬP LỖI: Database sập khi lưu Order (bước cuối cùng)
        // Dùng lenient() vì có thể code fail trước đó (nhưng ở đây logic đúng nên sẽ chạy tới save)
        doThrow(new RuntimeException("DB Connection Failed")).when(mockOrderRepository).save(any(OrderData.class));

        ArgumentCaptor<CancelOrderResponseData> captor = ArgumentCaptor.forClass(CancelOrderResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockOutputBoundary).present(captor.capture());
        CancelOrderResponseData response = captor.getValue();

        assertFalse(response.success, "Phải trả về thất bại khi DB lỗi");
        assertTrue(response.message.contains("Lỗi hệ thống"), "Message thực tế: " + response.message);
    }
}