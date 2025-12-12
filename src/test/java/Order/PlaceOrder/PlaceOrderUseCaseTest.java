package Order.PlaceOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlaceOrderUseCaseTest {

    @Mock private IOrderRepository orderRepository;
    @Mock private IDeviceRepository deviceRepository;
    @Mock private IAuthTokenValidator tokenValidator;
    @Mock private IOrderIdGenerator idGenerator;
    @Mock private IDeviceMapper deviceMapper;
    @Mock private PlaceOrderOutputBoundary outputBoundary;

    private PlaceOrderUseCase useCase;

    // Data mẫu
    private String userToken = "valid_token";
    private String userId = "user-123";
    private String validAddress = "123 Street, City";
    private String paymentMethod = "COD";
    private String laptopId = "laptop-001";

    private PlaceOrderRequestData request;

    @BeforeEach
    void setUp() {
        useCase = new PlaceOrderUseCase(orderRepository, deviceRepository, tokenValidator, idGenerator, deviceMapper, outputBoundary);

        // Init request cơ bản hợp lệ
        Map<String, Integer> cart = new HashMap<>();
        cart.put(laptopId, 1); // Mua 1 cái laptop
        request = new PlaceOrderRequestData(userToken, validAddress, cart);
    }

    private void mockAuth() {
        AuthPrincipal principal = new AuthPrincipal(userId, "user@test.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(userToken)).thenReturn(principal);
    }

    // Case: Token hết hạn
    @Test
    void testExecute_Fail_InvalidToken() {
        // Arrange
        when(tokenValidator.validate(anyString())).thenThrow(new SecurityException("Token hết hạn"));

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(outputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Token hết hạn", captor.getValue().message);
    }
    
    // Case: Địa chỉ giao hàng rỗng
    @Test
    void testExecute_Fail_EmptyAddress() {
        // Arrange
        mockAuth();
        // Request với địa chỉ rỗng
        request = new PlaceOrderRequestData(userToken, "", request.cartItems);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Message từ Order.validateOrderInfo
        assertTrue(captor.getValue().message.contains("Địa chỉ giao hàng không được để trống"));
    }
    
    @Test
    @DisplayName("Fail: Giỏ hàng rỗng")
    void testExecute_Fail_EmptyCart() {
        mockAuth();
        
        request = new PlaceOrderRequestData(userToken, validAddress, Collections.emptyMap());

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(outputBoundary).present(captor.capture());

        PlaceOrderResponseData response = captor.getValue();

        assertFalse(response.success);
        assertTrue(response.message.contains("Giỏ hàng")); 

        verify(orderRepository, never()).save(any());
        verify(deviceRepository, never()).save(any());
    }

//    // Case: Sản phẩm không tồn tại
//    @Test
//    void testExecute_Fail_ProductNotFound() {
//        // Arrange
//        mockAuth();
//        when(idGenerator.generate()).thenReturn("ORDER-001");
//        
//        // Giả lập tìm không thấy thiết bị
//        when(deviceRepository.findById(laptopId)).thenReturn(null);
//
//        // Act
//        useCase.execute(request);
//
//        // Assert
//        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
//        verify(outputBoundary).present(captor.capture());
//        
//        assertFalse(captor.getValue().success);
//        assertTrue(captor.getValue().message.contains("Sản phẩm không tồn tại"));
//    }

    // Case: Số lượng mua lớn hơn tồn kho
    @Test
    void testExecute_Fail_OutOfStock() {
        // Arrange
        mockAuth();
        when(idGenerator.generate()).thenReturn("ORDER-001");

        // Request mua 10 cái
        Map<String, Integer> cart = new HashMap<>();
        cart.put(laptopId, 10);
        request = new PlaceOrderRequestData(userToken, validAddress, cart);

        // Mock Device Data từ DB (DTO)
        DeviceData laptopDTO = new DeviceData();
        laptopDTO.id = laptopId;
        laptopDTO.type = "LAPTOP";
        laptopDTO.stockQuantity = 5; // Chỉ còn 5 cái
        when(deviceRepository.findById(laptopId)).thenReturn(laptopDTO);

        // Mock Mapper: Chuyển DTO thành Entity thật để chạy logic validateStock
        Laptop laptopEntity = new Laptop(laptopId, "Dell XPS", "Desc", BigDecimal.valueOf(1000), 5, "cat1", "NEW", "img", Instant.now(), Instant.now(), "i7", "16GB", "512GB", 15.6);
        when(deviceMapper.toEntity(laptopDTO)).thenReturn(laptopEntity);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertNotNull(captor.getValue().message); 
        
        verify(orderRepository, never()).save(any());
    }

    // Case: Thành công
    @Test
    void testExecute_Success() {
        // Arrange
        mockAuth();
        String orderId = "ORDER-SUCCESS-001";
        when(idGenerator.generate()).thenReturn(orderId);

        // Setup Device: Tồn kho 10, mua 2
        int initialStock = 10;
        int buyQuantity = 2;
        BigDecimal price = BigDecimal.valueOf(1000); // Giá 1000

        Map<String, Integer> cart = new HashMap<>();
        cart.put(laptopId, buyQuantity);
        request = new PlaceOrderRequestData(userToken, validAddress, cart);

        // Mock Data
        DeviceData laptopDTO = new DeviceData();
        laptopDTO.id = laptopId;
        laptopDTO.type = "LAPTOP";
        laptopDTO.stockQuantity = initialStock;
        
        when(deviceRepository.findById(laptopId)).thenReturn(laptopDTO);

        // Mock Mapper: Entity Laptop thật
        Laptop laptopEntity = new Laptop(laptopId, "Dell XPS", "Desc", price, initialStock, "cat1", "NEW", "img", Instant.now(), Instant.now(), "i7", "16GB", "512GB", 15.6);
        when(deviceMapper.toEntity(laptopDTO)).thenReturn(laptopEntity);

        // Mock Mapper ngược lại: Entity -> DTO (để save lại kho)
        DeviceData updatedDTO = new DeviceData();
        updatedDTO.id = laptopId;
        updatedDTO.stockQuantity = initialStock - buyQuantity; // 8
        when(deviceMapper.toDTO(laptopEntity)).thenReturn(updatedDTO);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<PlaceOrderResponseData> responseCaptor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(outputBoundary).present(responseCaptor.capture());
        PlaceOrderResponseData response = responseCaptor.getValue();

        // 1. Kiểm tra Output
        assertTrue(response.success);
        assertEquals("Đặt hàng thành công!", response.message);
        assertEquals(orderId, response.orderId);
        // Tổng tiền = 1000 * 2 = 2000
        assertEquals(price.multiply(BigDecimal.valueOf(buyQuantity)), response.totalAmount);

        // 2. Kiểm tra Logic Trừ kho
        ArgumentCaptor<DeviceData> deviceCaptor = ArgumentCaptor.forClass(DeviceData.class);
        verify(deviceRepository).save(deviceCaptor.capture());
        // Repository phải được gọi để lưu số lượng tồn kho mới (8)
        assertEquals(initialStock - buyQuantity, deviceCaptor.getValue().stockQuantity);

        // 3. Kiểm tra Lưu Order
        ArgumentCaptor<OrderData> orderCaptor = ArgumentCaptor.forClass(OrderData.class);
        verify(orderRepository).save(orderCaptor.capture());
        OrderData savedOrder = orderCaptor.getValue();
        
        assertEquals(orderId, savedOrder.id);
        assertEquals("COD", savedOrder.paymentMethod);
        assertEquals(userId, savedOrder.userId);
        assertEquals("PENDING", savedOrder.status);
        assertEquals(1, savedOrder.items.size()); // Có 1 loại sản phẩm
    }
}