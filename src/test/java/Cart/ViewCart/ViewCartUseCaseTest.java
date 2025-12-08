package Cart.ViewCart;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.ProductAvailability;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Cart.CartData;
import cgx.com.usecase.Cart.CartItemData;
import cgx.com.usecase.Cart.ICartRepository;
import cgx.com.usecase.Cart.ViewCart.ViewCartOutputBoundary;
import cgx.com.usecase.Cart.ViewCart.ViewCartRequestData;
import cgx.com.usecase.Cart.ViewCart.ViewCartResponseData;
import cgx.com.usecase.Cart.ViewCart.ViewCartUseCase;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

@ExtendWith(MockitoExtension.class)
public class ViewCartUseCaseTest {

    @Mock private ICartRepository mockCartRepo;
    @Mock private IDeviceRepository mockDeviceRepo;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IDeviceMapper mockDeviceMapper;
    @Mock private ViewCartOutputBoundary mockOutputBoundary;
    @Mock private ComputerDevice mockDeviceEntity; // Mock Entity để kiểm soát logic checkAvailability

    private ViewCartUseCase useCase;
    private AuthPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        useCase = new ViewCartUseCase(mockCartRepo, mockDeviceRepo, mockTokenValidator, mockDeviceMapper, mockOutputBoundary);
        userPrincipal = new AuthPrincipal("user-1", "test@mail.com", UserRole.CUSTOMER);
    }

    // =========================================================================
    // KỊCH BẢN 1: AUTHENTICATION FAILURE (Kiểm tra đầu vào)
    // =========================================================================
    @Test
    @DisplayName("KB1: Thất bại do chưa đăng nhập (Token null)")
    void test_KB1_Fail_NoAuth() {
        ViewCartRequestData input = new ViewCartRequestData(null);

        useCase.execute(input);

        ArgumentCaptor<ViewCartResponseData> captor = ArgumentCaptor.forClass(ViewCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Vui lòng đăng nhập để xem giỏ hàng.", captor.getValue().message);
    }

    // =========================================================================
    // KỊCH BẢN 2: GIỎ HÀNG TRỐNG (Happy Path đơn giản nhất)
    // =========================================================================
    @Test
    @DisplayName("KB2: Thành công - Giỏ hàng trống (Chưa mua gì)")
    void test_KB2_Success_EmptyCart() {
        ViewCartRequestData input = new ViewCartRequestData("token");
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockCartRepo.findByUserId("user-1")).thenReturn(null); // Repo trả về null

        useCase.execute(input);

        ArgumentCaptor<ViewCartResponseData> captor = ArgumentCaptor.forClass(ViewCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertTrue(captor.getValue().success);
        assertEquals("Giỏ hàng của bạn đang trống.", captor.getValue().message);
        assertTrue(captor.getValue().items.isEmpty());
    }

    // =========================================================================
    // KỊCH BẢN 3: SẢN PHẨM CÓ SẴN (AVAILABLE) - Real-time Update
    // =========================================================================
    @Test
    @DisplayName("KB3: Thành công - Sản phẩm có hàng, giá cập nhật realtime")
    void test_KB3_Success_Available() {
        ViewCartRequestData input = new ViewCartRequestData("token");
        
        // 1. Mock Auth
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        
        // 2. Mock Cart DB (User có 2 cái Laptop trong giỏ)
        CartData cartData = new CartData();
        cartData.items = Collections.singletonList(new CartItemData("lap-1", 2));
        when(mockCartRepo.findByUserId("user-1")).thenReturn(cartData);

        // 3. Mock Device DB (Sản phẩm ACTIVE)
        DeviceData deviceDTO = new DeviceData(); 
        deviceDTO.id = "lap-1";
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceDTO);

        // 4. Mock Entity & Logic
        when(mockDeviceMapper.toEntity(deviceDTO)).thenReturn(mockDeviceEntity);
        when(mockDeviceEntity.getName()).thenReturn("Laptop Dell");
        when(mockDeviceEntity.getPrice()).thenReturn(new BigDecimal("15000000")); // Giá hiện tại 15tr
        // Entity trả về AVAILABLE
        when(mockDeviceEntity.checkAvailability(2)).thenReturn(ProductAvailability.AVAILABLE);

        // ACT
        useCase.execute(input);

        // ASSERT
        ArgumentCaptor<ViewCartResponseData> captor = ArgumentCaptor.forClass(ViewCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        ViewCartResponseData response = captor.getValue();

        assertTrue(response.success);
        assertEquals(ProductAvailability.AVAILABLE, response.items.get(0).availabilityStatus);
        // Tổng tiền = 2 * 15tr = 30tr
        assertEquals(new BigDecimal("30000000"), response.totalCartPrice);
    }

    // =========================================================================
    // KỊCH BẢN 4: KHÔNG ĐỦ SỐ LƯỢNG (NOT_ENOUGH_STOCK)
    // =========================================================================
    @Test
    @DisplayName("KB4: Thành công - Cảnh báo không đủ số lượng tồn kho")
    void test_KB4_Success_NotEnoughStock() {
        ViewCartRequestData input = new ViewCartRequestData("token");
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        
        // Giỏ mua 5 cái
        CartData cartData = new CartData();
        cartData.items = Collections.singletonList(new CartItemData("lap-1", 5));
        when(mockCartRepo.findByUserId("user-1")).thenReturn(cartData);

        // Device DB
        DeviceData deviceDTO = new DeviceData();
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceDTO);

        // Entity trả về NOT_ENOUGH_STOCK
        when(mockDeviceMapper.toEntity(deviceDTO)).thenReturn(mockDeviceEntity);
        when(mockDeviceEntity.getPrice()).thenReturn(new BigDecimal("100"));
        when(mockDeviceEntity.checkAvailability(5)).thenReturn(ProductAvailability.NOT_ENOUGH_STOCK);
        when(mockDeviceEntity.getStockQuantity()).thenReturn(3); // Kho thực tế chỉ còn 3

        useCase.execute(input);

        ArgumentCaptor<ViewCartResponseData> captor = ArgumentCaptor.forClass(ViewCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        ViewCartResponseData response = captor.getValue();

        assertEquals(ProductAvailability.NOT_ENOUGH_STOCK, response.items.get(0).availabilityStatus);
        assertEquals(3, response.items.get(0).currentStock); // Trả về số tồn kho để UI báo
    }

    // =========================================================================
    // KỊCH BẢN 5: HẾT HÀNG (OUT_OF_STOCK)
    // =========================================================================
    @Test
    @DisplayName("KB5: Thành công - Báo hết hàng (Stock=0)")
    void test_KB5_Success_OutOfStock() {
        // Setup tương tự, nhưng Entity trả về OUT_OF_STOCK
        ViewCartRequestData input = new ViewCartRequestData("token");
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        
        CartData cartData = new CartData();
        cartData.items = Collections.singletonList(new CartItemData("lap-1", 1));
        when(mockCartRepo.findByUserId("user-1")).thenReturn(cartData);

        DeviceData deviceDTO = new DeviceData();
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceDTO);

        when(mockDeviceMapper.toEntity(deviceDTO)).thenReturn(mockDeviceEntity);
        when(mockDeviceEntity.getPrice()).thenReturn(BigDecimal.TEN);
        when(mockDeviceEntity.checkAvailability(1)).thenReturn(ProductAvailability.OUT_OF_STOCK);

        useCase.execute(input);

        ArgumentCaptor<ViewCartResponseData> captor = ArgumentCaptor.forClass(ViewCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertEquals(ProductAvailability.OUT_OF_STOCK, captor.getValue().items.get(0).availabilityStatus);
    }

    // =========================================================================
    // KỊCH BẢN 6: SẢN PHẨM NGỪNG KINH DOANH / BỊ XÓA (DISCONTINUED)
    // =========================================================================
    @Test
    @DisplayName("KB6: Thành công - SP bị xóa cứng khỏi DB")
    void test_KB6_Success_ProductDeleted() {
        ViewCartRequestData input = new ViewCartRequestData("token");
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        
        CartData cartData = new CartData();
        cartData.items = Collections.singletonList(new CartItemData("deleted-id", 1));
        when(mockCartRepo.findByUserId("user-1")).thenReturn(cartData);

        // Repo trả về NULL -> Use Case tự xử lý thành DISCONTINUED
        when(mockDeviceRepo.findById("deleted-id")).thenReturn(null);

        useCase.execute(input);

        ArgumentCaptor<ViewCartResponseData> captor = ArgumentCaptor.forClass(ViewCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertEquals("Sản phẩm không còn tồn tại", captor.getValue().items.get(0).deviceName);
        assertEquals(ProductAvailability.DISCONTINUED, captor.getValue().items.get(0).availabilityStatus);
    }

    // =========================================================================
    // KỊCH BẢN 7: LỖI HỆ THỐNG (EXCEPTION)
    // =========================================================================
    @Test
    @DisplayName("KB7: Thất bại - Lỗi hệ thống bất ngờ (DB chết)")
    void test_KB7_Fail_SystemError() {
        ViewCartRequestData input = new ViewCartRequestData("token");
        when(mockTokenValidator.validate("token")).thenThrow(new RuntimeException("Connection Timeout"));

        useCase.execute(input);

        ArgumentCaptor<ViewCartResponseData> captor = ArgumentCaptor.forClass(ViewCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Đã xảy ra lỗi hệ thống"));
        assertEquals(BigDecimal.ZERO, captor.getValue().totalCartPrice); // Đảm bảo reset giá về 0
    }
}