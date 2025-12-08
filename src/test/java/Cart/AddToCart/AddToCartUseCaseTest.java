package Cart.AddToCart;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.Cart.CartData;
import cgx.com.usecase.Cart.CartItemData;
import cgx.com.usecase.Cart.ICartRepository;
import cgx.com.usecase.Cart.AddToCart.AddToCartOutputBoundary;
import cgx.com.usecase.Cart.AddToCart.AddToCartRequestData;
import cgx.com.usecase.Cart.AddToCart.AddToCartResponseData;
import cgx.com.usecase.Cart.AddToCart.AddToCartUseCase;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

@ExtendWith(MockitoExtension.class)
public class AddToCartUseCaseTest {

    // 1. Mock Dependencies
    @Mock private ICartRepository mockCartRepository;
    @Mock private IDeviceRepository mockDeviceRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private AddToCartOutputBoundary mockOutputBoundary;

    // 2. Class under test
    private AddToCartUseCase useCase;

    // 3. Test Data Helpers
    private AuthPrincipal userPrincipal;
    private DeviceData deviceData;

    @BeforeEach
    void setUp() {
        useCase = new AddToCartUseCase(mockCartRepository, mockDeviceRepository, mockTokenValidator, mockOutputBoundary);
        
        // Giả lập User
        userPrincipal = new AuthPrincipal("user-1", "test@mail.com", UserRole.CUSTOMER);
        
        // Giả lập Sản phẩm (Mặc định là có hàng)
        deviceData = new DeviceData();
        deviceData.id = "lap-1";
        deviceData.name = "Laptop Gaming";
        deviceData.price = new BigDecimal("20000000");
        deviceData.stockQuantity = 10; // Kho có 10 cái
        deviceData.status = "ACTIVE";
    }

    // =========================================================================
    // KỊCH BẢN 3 (THÀNH CÔNG): SP CÓ TRONG KHO & ĐỦ SỐ LƯỢNG
    // Flow trong ảnh: 2.1 -> 2.2.1 -> 2.2.2 -> 3
    // =========================================================================

    @Test
    @DisplayName("Kịch bản 3: Thêm mới thành công (Giỏ hàng đang trống)")
    void test_KB3_Success_EmptyCart() {
        // GIVEN: User mua 2 cái, Giỏ đang trống
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 2);

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(deviceData);
        when(mockCartRepository.findByUserId("user-1")).thenReturn(null); // Chưa có giỏ

        // WHEN
        useCase.execute(input);

        // THEN
        ArgumentCaptor<CartData> cartCaptor = ArgumentCaptor.forClass(CartData.class);
        ArgumentCaptor<AddToCartResponseData> resCaptor = ArgumentCaptor.forClass(AddToCartResponseData.class);

        // 1. Verify dữ liệu lưu xuống DB
        verify(mockCartRepository).save(cartCaptor.capture());
        CartData savedCart = cartCaptor.getValue();
        assertEquals(1, savedCart.items.size());
        assertEquals(2, savedCart.items.get(0).quantity);
        assertEquals(new BigDecimal("40000000"), savedCart.totalEstimatedPrice); // 2 * 20tr

        // 2. Verify phản hồi
        verify(mockOutputBoundary).present(resCaptor.capture());
        assertTrue(resCaptor.getValue().success);
        assertEquals("Đã thêm sản phẩm vào giỏ hàng.", resCaptor.getValue().message);
        assertEquals(2, resCaptor.getValue().totalItemsInCart);
    }

    @Test
    @DisplayName("Kịch bản 3 (Mở rộng): Cộng dồn số lượng thành công")
    void test_KB3_Success_MergeQuantity() {
        // GIVEN: Kho 10. Giỏ đã có 3. Mua thêm 2 -> Tổng 5 (<= 10) -> OK
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 2);

        // Giả lập giỏ hàng cũ đã có 3 item
        CartData existingCart = new CartData();
        existingCart.userId = "user-1";
        existingCart.items = new ArrayList<>(Collections.singletonList(new CartItemData("lap-1", 3)));
        existingCart.totalEstimatedPrice = new BigDecimal("60000000");

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(deviceData);
        when(mockCartRepository.findByUserId("user-1")).thenReturn(existingCart);

        // WHEN
        useCase.execute(input);

        // THEN
        ArgumentCaptor<CartData> cartCaptor = ArgumentCaptor.forClass(CartData.class);
        verify(mockCartRepository).save(cartCaptor.capture());
        CartData savedCart = cartCaptor.getValue();

        assertEquals(1, savedCart.items.size()); // Vẫn là 1 dòng
        assertEquals(5, savedCart.items.get(0).quantity); // 3 + 2 = 5
        assertEquals(new BigDecimal("100000000"), savedCart.totalEstimatedPrice); // 60tr + 40tr
    }

    // =========================================================================
    // KỊCH BẢN 2 (LỖI): SỐ LƯỢNG KHO KHÔNG ĐỦ
    // Flow trong ảnh: 2.1 -> 2.2 -> Lỗi
    // =========================================================================

    @Test
    @DisplayName("Kịch bản 2: Lỗi do số lượng yêu cầu lớn hơn tồn kho (Mua mới)")
    void test_KB2_Fail_StockNotEnough_New() {
        // GIVEN: Kho 10. Mua 15.
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 15);

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(deviceData); // Stock 10
        when(mockCartRepository.findByUserId("user-1")).thenReturn(null);

        // WHEN
        useCase.execute(input);

        // THEN
        ArgumentCaptor<AddToCartResponseData> resCaptor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        
        verify(mockCartRepository, never()).save(any()); // Không được lưu
        verify(mockOutputBoundary).present(resCaptor.capture());
        
        assertFalse(resCaptor.getValue().success);
        // Message từ Entity Cart ném ra
        assertTrue(resCaptor.getValue().message.contains("Số lượng vượt quá tồn kho"));
    }

    @Test
    @DisplayName("Kịch bản 2 (Mở rộng): Lỗi do Cộng dồn vượt quá tồn kho")
    void test_KB2_Fail_StockNotEnough_Merge() {
        // GIVEN: Kho 10. Giỏ đã có 8. Mua thêm 3 -> Tổng 11 > 10 -> Lỗi
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 3);

        CartData existingCart = new CartData();
        existingCart.userId = "user-1";
        existingCart.items = new ArrayList<>(Collections.singletonList(new CartItemData("lap-1", 8)));

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(deviceData);
        when(mockCartRepository.findByUserId("user-1")).thenReturn(existingCart);

        // WHEN
        useCase.execute(input);

        // THEN
        ArgumentCaptor<AddToCartResponseData> resCaptor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        assertFalse(resCaptor.getValue().success);
    }

    // =========================================================================
    // KỊCH BẢN 1 (LỖI): SP KHÔNG CÓ TRONG KHO (HOẶC KHÔNG TỒN TẠI)
    // Flow trong ảnh: 2 -> Lỗi
    // =========================================================================

    @Test
    @DisplayName("Kịch bản 1: Lỗi do sản phẩm không tồn tại")
    void test_KB1_Fail_ProductNotFound() {
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-999", 1);
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepository.findById("lap-999")).thenReturn(null);

        useCase.execute(input);

        ArgumentCaptor<AddToCartResponseData> resCaptor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        
        assertFalse(resCaptor.getValue().success);
        assertEquals("Sản phẩm không tồn tại.", resCaptor.getValue().message);
    }

    @Test
    @DisplayName("Kịch bản 1 (Variant): Lỗi do sản phẩm hết hàng (Stock=0)")
    void test_KB1_Fail_OutOfStock() {
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 1);
        deviceData.stockQuantity = 0; // Hết hàng

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenReturn(deviceData);

        useCase.execute(input);

        ArgumentCaptor<AddToCartResponseData> resCaptor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        
        assertFalse(resCaptor.getValue().success);
        // Kiểm tra đúng message logic check nhanh trong UseCase
        assertTrue(resCaptor.getValue().message.contains("hết hàng") || resCaptor.getValue().message.contains("Số lượng vượt quá"));
    }

    // =========================================================================
    // CÁC TRƯỜNG HỢP VALIDATION & SYSTEM ERROR
    // =========================================================================

    @Test
    @DisplayName("Fail: Chưa đăng nhập (Auth Token null)")
    void test_Fail_NoAuth() {
        AddToCartRequestData input = new AddToCartRequestData(null, "lap-1", 1);
        
        useCase.execute(input);

        ArgumentCaptor<AddToCartResponseData> resCaptor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        assertFalse(resCaptor.getValue().success);
        assertEquals("Vui lòng đăng nhập để mua hàng.", resCaptor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Số lượng mua <= 0")
    void test_Fail_InvalidQuantity() {
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 0);
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);

        useCase.execute(input);

        ArgumentCaptor<AddToCartResponseData> resCaptor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        assertFalse(resCaptor.getValue().success);
        assertEquals("Số lượng phải lớn hơn 0.", resCaptor.getValue().message); // Lỗi từ Entity CartItem
    }

    @Test
    @DisplayName("Fail: Lỗi hệ thống bất ngờ (DB chết)")
    void test_Fail_SystemError() {
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 1);
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepository.findById("lap-1")).thenThrow(new RuntimeException("DB Connection Fail"));

        useCase.execute(input);

        ArgumentCaptor<AddToCartResponseData> resCaptor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        
        assertFalse(resCaptor.getValue().success);
        assertTrue(resCaptor.getValue().message.contains("Lỗi hệ thống"));
    }
}
