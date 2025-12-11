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

    // 1. Mock Dependencies
    @Mock private IOrderRepository mockOrderRepository;
    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IOrderIdGenerator mockIdGenerator;
    @Mock private IDeviceMapper mockDeviceMapper;
    @Mock private PlaceOrderOutputBoundary mockOutputBoundary;
    
    // Mock Entity (Để verify logic trừ kho)
    @Mock private ComputerDevice mockDeviceEntity;

    // 2. Class Under Test
    private PlaceOrderUseCase useCase;

    // 3. Test Data
    private AuthPrincipal userPrincipal;
    private PlaceOrderRequestData validRequest;
    private DeviceData deviceData;

    @BeforeEach
    void setUp() {
        useCase = new PlaceOrderUseCase(mockOrderRepository, mockDeviceRepository, mockTokenValidator, mockIdGenerator, mockDeviceMapper, mockOutputBoundary);
        
        userPrincipal = new AuthPrincipal("user-1", "test@mail.com", UserRole.CUSTOMER);
        
        // Request hợp lệ (Mua 1 cái Laptop)
        Map<String, Integer> cartItems = new HashMap<>();
        cartItems.put("lap-1", 1);
        validRequest = new PlaceOrderRequestData("token", "Hanoi", cartItems, "COD");

        // Device Data giả lập trong DB
        deviceData = new DeviceData();
        deviceData.id = "lap-1";
        deviceData.price = new BigDecimal("20000000");
        deviceData.stockQuantity = 10;
        deviceData.status = "ACTIVE";
    }

    // =========================================================================
    // KỊCH BẢN THÀNH CÔNG (HAPPY PATH)
    // =========================================================================

    @Test
    @DisplayName("Thành công: Đặt hàng hợp lệ, kho đủ hàng")
    void test_Execute_Success() {
        // GIVEN
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockIdGenerator.generate()).thenReturn("order-123");
        
        // Mock Device
        when(mockDeviceRepository.findById("lap-1")).thenReturn(deviceData);
        when(mockDeviceMapper.toEntity(deviceData)).thenReturn(mockDeviceEntity);
        
        // Mock Entity Behavior
        when(mockDeviceEntity.getId()).thenReturn("lap-1");
        when(mockDeviceEntity.getPrice()).thenReturn(new BigDecimal("20000000"));
        // Mock Mapper trả về DTO sau khi trừ kho
        DeviceData updatedDeviceData = new DeviceData();
        when(mockDeviceMapper.toDTO(mockDeviceEntity)).thenReturn(updatedDeviceData);

        // WHEN
        useCase.execute(validRequest);

        // THEN
        // 1. Verify Entity được gọi hàm trừ kho
        verify(mockDeviceEntity).validateStock(1);
        verify(mockDeviceEntity).minusStock(1);
        
        // 2. Verify Device được lưu lại (Cập nhật tồn kho)
        verify(mockDeviceRepository).save(updatedDeviceData);

        // 3. Verify Order được lưu
        ArgumentCaptor<OrderData> orderCaptor = ArgumentCaptor.forClass(OrderData.class);
        verify(mockOrderRepository).save(orderCaptor.capture());
        OrderData savedOrder = orderCaptor.getValue();
        assertEquals("user-1", savedOrder.userId);
        assertEquals("COD", savedOrder.paymentMethod);
        assertEquals(1, savedOrder.items.size());

        // 4. Verify Phản hồi thành công
        ArgumentCaptor<PlaceOrderResponseData> responseCaptor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(mockOutputBoundary).present(responseCaptor.capture());
        assertTrue(responseCaptor.getValue().success);
        assertEquals("order-123", responseCaptor.getValue().orderId);
    }

    // =========================================================================
    // KỊCH BẢN THẤT BẠI - VALIDATION (Đầu vào sai)
    // =========================================================================

    @Test
    @DisplayName("Thất bại: Token rỗng")
    void test_Fail_EmptyToken() {
        PlaceOrderRequestData input = new PlaceOrderRequestData(null, "HN", new HashMap<>(), "COD");
        
        useCase.execute(input);

        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Auth Token không được để trống.", captor.getValue().message);
    }

    @Test
    @DisplayName("Thất bại: Địa chỉ rỗng (Lỗi từ Entity Order)")
    void test_Fail_EmptyAddress() {
        PlaceOrderRequestData input = new PlaceOrderRequestData("token", "", validRequest.cartItems, "COD");
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        // Lưu ý: Order.validateOrderInfo sẽ ném IllegalArgumentException

        useCase.execute(input);

        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Địa chỉ giao hàng không được để trống.", captor.getValue().message);
    }

    @Test
    @DisplayName("Thất bại: Phương thức thanh toán không hợp lệ")
    void test_Fail_InvalidPaymentMethod() {
        PlaceOrderRequestData input = new PlaceOrderRequestData("token", "HN", validRequest.cartItems, "BITCOIN");
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        
        useCase.execute(input);

        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        // Lỗi này ném ra từ PaymentMethod.valueOf() hoặc custom validate trong Entity Order
        assertTrue(captor.getValue().message.contains("hợp lệ") || captor.getValue().message.contains("No enum constant"));
    }

    @Test
    @DisplayName("Thất bại: Giỏ hàng rỗng")
    void test_Fail_EmptyCart() {
        PlaceOrderRequestData input = new PlaceOrderRequestData("token", "HN", Collections.emptyMap(), "COD");
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);

        useCase.execute(input);

        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Giỏ hàng không được để trống.", captor.getValue().message);
    }

    // =========================================================================
    // KỊCH BẢN THẤT BẠI - LOGIC & DATA (Kho, Sản phẩm)
    // =========================================================================

    @Test
    @DisplayName("Thất bại: Sản phẩm không tồn tại trong DB")
    void test_Fail_ProductNotFound() {
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockIdGenerator.generate()).thenReturn("order-1");
        
        // Mock DB trả về null
        when(mockDeviceRepository.findById("lap-1")).thenReturn(null);

        useCase.execute(validRequest);

        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Sản phẩm không tồn tại: lap-1", captor.getValue().message);
    }

    @Test
    @DisplayName("Thất bại: Hết hàng / Không đủ tồn kho")
    void test_Fail_OutOfStock() {
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockIdGenerator.generate()).thenReturn("order-1");
        
        when(mockDeviceRepository.findById("lap-1")).thenReturn(deviceData);
        when(mockDeviceMapper.toEntity(deviceData)).thenReturn(mockDeviceEntity);
        
        // Giả lập Entity ném lỗi khi validate stock
        doThrow(new IllegalArgumentException("Sản phẩm không đủ hàng.")).when(mockDeviceEntity).validateStock(1);

        useCase.execute(validRequest);

        // Verify KHÔNG lưu Order
        verify(mockOrderRepository, never()).save(any());

        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Sản phẩm không đủ hàng.", captor.getValue().message);
    }

    // =========================================================================
    // KỊCH BẢN THẤT BẠI - HỆ THỐNG
    // =========================================================================

    @Test
    @DisplayName("Thất bại: Lỗi hệ thống bất ngờ (DB chết)")
    void test_Fail_SystemError() {
        when(mockTokenValidator.validate("token")).thenThrow(new RuntimeException("DB Connection Timeout"));

        useCase.execute(validRequest);

        ArgumentCaptor<PlaceOrderResponseData> captor = ArgumentCaptor.forClass(PlaceOrderResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Lỗi hệ thống"));
    }
}