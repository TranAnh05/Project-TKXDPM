package Order.PlaceOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Laptop;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderIdGenerator;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageOrder.PlaceOrder.PlaceOrderOutputBoundary;
import cgx.com.usecase.ManageOrder.PlaceOrder.PlaceOrderRequestData;
import cgx.com.usecase.ManageOrder.PlaceOrder.PlaceOrderResponseData;
import cgx.com.usecase.ManageOrder.PlaceOrder.PlaceOrderUseCase;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlaceOrderUseCaseTest {

    @Mock private IOrderRepository mockOrderRepository;
    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IOrderIdGenerator mockIdGenerator;
    @Mock private IDeviceMapper mockDeviceMapper; // Mock Mapper Interface
    @Mock private PlaceOrderOutputBoundary mockOutputBoundary;

    private PlaceOrderUseCase useCase;
    private AuthPrincipal customerPrincipal;
    private DeviceData laptopData;
    private Laptop laptopEntity;

    @BeforeEach
    void setUp() {
        useCase = new PlaceOrderUseCase(
            mockOrderRepository, mockDeviceRepository, mockTokenValidator, 
            mockIdGenerator, mockDeviceMapper, mockOutputBoundary
        );
        
        customerPrincipal = new AuthPrincipal("user-1", "user@test.com", UserRole.CUSTOMER);
        
        // Dữ liệu DTO giả lập từ DB
        laptopData = new DeviceData();
        laptopData.id = "lap-1";
        laptopData.name = "Gaming Laptop";
        laptopData.price = new BigDecimal("1000");
        laptopData.stockQuantity = 5; 
        laptopData.status = "ACTIVE";
        laptopData.type = "LAPTOP"; // Trường mới
        laptopData.cpu = "Intel";
        laptopData.screenSize = 15.6;

        // Entity giả lập mà Mapper sẽ trả về
        laptopEntity = new Laptop(
            "lap-1", "Gaming Laptop", "Desc", new BigDecimal("1000"), 5, 
            "cat-1", "ACTIVE", "url", Instant.now(), Instant.now(),
            "Intel", "16GB", "1TB", 15.6
        );
    }

    // Case 1: Thành công
    @Test
    void test_execute_success() {
        PlaceOrderRequestData input = new PlaceOrderRequestData("token", "Addr", Map.of("lap-1", 2));

        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockIdGenerator.generate()).thenReturn("order-123");
        when(mockDeviceRepository.findById("lap-1")).thenReturn(laptopData);
        
        // Mock Mapper: Khi nhận DTO -> Trả về Entity
        when(mockDeviceMapper.toEntity(laptopData)).thenReturn(laptopEntity);
        // Mock Mapper: Khi nhận Entity (sau khi trừ kho) -> Trả về DTO (để lưu)
        when(mockDeviceMapper.toDTO(any(ComputerDevice.class))).thenReturn(laptopData);

        ArgumentCaptor<OrderData> orderCaptor = ArgumentCaptor.forClass(OrderData.class);
        ArgumentCaptor<DeviceData> deviceCaptor = ArgumentCaptor.forClass(DeviceData.class);
        ArgumentCaptor<PlaceOrderResponseData> responseCaptor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);

        useCase.execute(input);

        verify(mockOrderRepository).save(orderCaptor.capture());
        verify(mockDeviceRepository).save(deviceCaptor.capture());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertTrue(responseCaptor.getValue().success);
        assertEquals("order-123", responseCaptor.getValue().orderId);
        
        // Kiểm tra logic nghiệp vụ trên Entity: 5 - 2 = 3
        assertEquals(3, laptopEntity.getStockQuantity());
    }

    // Case 2: Hết hàng (Validation trong Entity)
    @Test
    void test_execute_failure_outOfStock() {
        PlaceOrderRequestData input = new PlaceOrderRequestData("token", "Addr", Map.of("lap-1", 10));

        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(laptopData);
        when(mockDeviceMapper.toEntity(laptopData)).thenReturn(laptopEntity);

        ArgumentCaptor<PlaceOrderResponseData> responseCaptor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);

        useCase.execute(input);

        verify(mockOrderRepository, never()).save(any());
        verify(mockOutputBoundary).present(responseCaptor.capture());

        assertFalse(responseCaptor.getValue().success);
        // LaptopEntity sẽ ném lỗi khi updateStock nếu số lượng không đủ (logic này nằm trong Entity)
        // Tuy nhiên, trong PlaceOrderUseCase hiện tại, chúng ta đang check thủ công trước khi gọi updateStock
        // để ném message rõ ràng hơn.
        assertTrue(responseCaptor.getValue().message.contains("không đủ hàng"));
    }

    // Case 3: Lỗi Mapper (Data lỗi, không map được)
    @Test
    void test_execute_failure_mappingError() {
        PlaceOrderRequestData input = new PlaceOrderRequestData("token", "Addr", Map.of("lap-1", 1));

        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(laptopData);
        // Giả lập Mapper trả về null (do data lỗi)
        when(mockDeviceMapper.toEntity(laptopData))
        	.thenThrow(new IllegalArgumentException("Dữ liệu sản phẩm lỗi (thiếu thông tin định danh loại)."));

        ArgumentCaptor<PlaceOrderResponseData> responseCaptor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Dữ liệu sản phẩm lỗi (thiếu thông tin định danh loại).", responseCaptor.getValue().message);
    }
    
    // Case 4: Sản phẩm không tồn tại
    @Test
    void test_execute_failure_notFound() {
        PlaceOrderRequestData input = new PlaceOrderRequestData("token", "Addr", Map.of("invalid", 1));
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockDeviceRepository.findById("invalid")).thenReturn(null);

        ArgumentCaptor<PlaceOrderResponseData> responseCaptor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Sản phẩm không tồn tại: invalid", responseCaptor.getValue().message);
    }

    // Case 5: DB Error
    @Test
    void test_execute_failure_dbError() {
        PlaceOrderRequestData input = new PlaceOrderRequestData("token", "Addr", Map.of("lap-1", 1));
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        when(mockIdGenerator.generate()).thenReturn("order-1");
        when(mockDeviceRepository.findById("lap-1")).thenReturn(laptopData);
        when(mockDeviceMapper.toEntity(laptopData)).thenReturn(laptopData != null ? laptopEntity : null); // Mock mapper
        when(mockDeviceMapper.toDTO(any())).thenReturn(laptopData);

        // Giả lập lỗi khi lưu Order
        lenient().doThrow(new RuntimeException("DB Crash")).when(mockOrderRepository).save(any(OrderData.class));

        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Lỗi hệ thống"));
    }
}