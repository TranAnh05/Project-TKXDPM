package Cart.RemoveFromCart;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import cgx.com.usecase.Cart.RemoveFromCart.RemoveFromCartOutputBoundary;
import cgx.com.usecase.Cart.RemoveFromCart.RemoveFromCartRequestData;
import cgx.com.usecase.Cart.RemoveFromCart.RemoveFromCartResponseData;
import cgx.com.usecase.Cart.RemoveFromCart.RemoveFromCartUseCase;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

@ExtendWith(MockitoExtension.class)
public class RemoveFromCartUseCaseTest {

    // 1. Mock Dependencies
    @Mock private ICartRepository mockCartRepo;
    @Mock private IDeviceRepository mockDeviceRepo;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private RemoveFromCartOutputBoundary mockOutputBoundary;

    // 2. Class under test
    private RemoveFromCartUseCase useCase;

    // 3. Test Data
    private AuthPrincipal userPrincipal;
    private CartData existingCart;
    private DeviceData deviceData;

    @BeforeEach
    void setUp() {
        useCase = new RemoveFromCartUseCase(mockCartRepo, mockDeviceRepo, mockTokenValidator, mockOutputBoundary);
        
        userPrincipal = new AuthPrincipal("user-1", "test@mail.com", UserRole.CUSTOMER);
        
        // Giỏ hàng giả lập: Đang có 1 món "lap-1", số lượng 2, tổng tiền 40tr
        existingCart = new CartData();
        existingCart.userId = "user-1";
        List<CartItemData> items = new ArrayList<>();
        items.add(new CartItemData("lap-1", 2));
        existingCart.items = items;
        existingCart.totalEstimatedPrice = new BigDecimal("40000000");

        // Sản phẩm giả lập
        deviceData = new DeviceData();
        deviceData.id = "lap-1";
        deviceData.price = new BigDecimal("20000000");
    }

    // =========================================================================
    // 1. KỊCH BẢN THẤT BẠI: LỖI AUTHENTICATION
    // =========================================================================
    @Test
    @DisplayName("Fail: Token rỗng hoặc null")
    void test_Fail_NoAuth() {
        RemoveFromCartRequestData input = new RemoveFromCartRequestData(null, "lap-1");
        
        useCase.execute(input);

        ArgumentCaptor<RemoveFromCartResponseData> captor = ArgumentCaptor.forClass(RemoveFromCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Vui lòng đăng nhập.", captor.getValue().message);
    }

    // =========================================================================
    // 2. KỊCH BẢN THẤT BẠI: LỖI INPUT (DEVICE ID)
    // =========================================================================
    @Test
    @DisplayName("Fail: DeviceId rỗng")
    void test_Fail_InvalidDeviceId() {
        RemoveFromCartRequestData input = new RemoveFromCartRequestData("token", "");
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);

        useCase.execute(input);

        ArgumentCaptor<RemoveFromCartResponseData> captor = ArgumentCaptor.forClass(RemoveFromCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("ID sản phẩm không được để trống.", captor.getValue().message);
    }

    // =========================================================================
    // 3. KỊCH BẢN THẤT BẠI: GIỎ HÀNG KHÔNG TỒN TẠI
    // =========================================================================
    @Test
    @DisplayName("Fail: User chưa có giỏ hàng (CartData null)")
    void test_Fail_CartNotFound() {
        RemoveFromCartRequestData input = new RemoveFromCartRequestData("token", "lap-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockCartRepo.findByUserId("user-1")).thenReturn(null); // Không tìm thấy giỏ

        useCase.execute(input);

        ArgumentCaptor<RemoveFromCartResponseData> captor = ArgumentCaptor.forClass(RemoveFromCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Giỏ hàng không tồn tại.", captor.getValue().message);
    }

    // =========================================================================
    // 4. KỊCH BẢN THẤT BẠI: LỖI HỆ THỐNG
    // =========================================================================
    @Test
    @DisplayName("Fail: DB Crash khi save")
    void test_Fail_SystemError() {
        RemoveFromCartRequestData input = new RemoveFromCartRequestData("token", "lap-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockCartRepo.findByUserId("user-1")).thenReturn(existingCart);
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceData);
        
        // Giả lập lỗi khi lưu
        doThrow(new RuntimeException("DB Connection Timeout")).when(mockCartRepo).save(any());

        useCase.execute(input);

        ArgumentCaptor<RemoveFromCartResponseData> captor = ArgumentCaptor.forClass(RemoveFromCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Lỗi hệ thống"));
    }

    // =========================================================================
    // 5. KỊCH BẢN THÀNH CÔNG (BIẾN THỂ): SẢN PHẨM ĐÃ BỊ XÓA KHỎI DB
    // =========================================================================
    @Test
    @DisplayName("Success: Xóa item dù sản phẩm gốc đã bị xóa khỏi DB (Giá = 0)")
    void test_Success_DeviceDeletedFromDB() {
        // GIVEN: Giỏ có "lap-1", nhưng bảng Device không còn "lap-1" (null)
        RemoveFromCartRequestData input = new RemoveFromCartRequestData("token", "lap-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockCartRepo.findByUserId("user-1")).thenReturn(existingCart);
        when(mockDeviceRepo.findById("lap-1")).thenReturn(null); // Sản phẩm bị xóa

        // WHEN
        useCase.execute(input);

        // THEN
        ArgumentCaptor<CartData> cartCaptor = ArgumentCaptor.forClass(CartData.class);
        verify(mockCartRepo).save(cartCaptor.capture());
        
        CartData savedCart = cartCaptor.getValue();
        assertTrue(savedCart.items.isEmpty()); // Đã xóa khỏi giỏ
        // Vì sản phẩm gốc null -> giá = 0 -> 40tr - 0 = 40tr (Không trừ được tiền, nhưng vẫn xóa item)
        // Đây là behavior chấp nhận được để làm sạch giỏ hàng rác.
        assertEquals(new BigDecimal("40000000"), savedCart.totalEstimatedPrice);
        
        ArgumentCaptor<RemoveFromCartResponseData> resCaptor = ArgumentCaptor.forClass(RemoveFromCartResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        assertTrue(resCaptor.getValue().success);
    }

    // =========================================================================
    // 6. KỊCH BẢN THÀNH CÔNG (CHUẨN): XÓA VÀ TRỪ TIỀN
    // =========================================================================
    @Test
    @DisplayName("Success: Xóa thành công và tính lại tổng tiền")
    void test_Success_Standard() {
        // GIVEN: Giỏ có 2 cái lap-1 (40tr). Device giá 20tr.
        RemoveFromCartRequestData input = new RemoveFromCartRequestData("token", "lap-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockCartRepo.findByUserId("user-1")).thenReturn(existingCart);
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceData); // Giá 20tr

        // WHEN
        useCase.execute(input);

        // THEN
        ArgumentCaptor<CartData> cartCaptor = ArgumentCaptor.forClass(CartData.class);
        verify(mockCartRepo).save(cartCaptor.capture());
        CartData savedCart = cartCaptor.getValue();
        
        // 1. Verify Item đã biến mất
        assertTrue(savedCart.items.isEmpty());
        
        // 2. Verify Tiền đã trừ: 40tr - (2 * 20tr) = 0
        assertEquals(0, savedCart.totalEstimatedPrice.compareTo(BigDecimal.ZERO)); 
        
        // 3. Verify Output
        ArgumentCaptor<RemoveFromCartResponseData> resCaptor = ArgumentCaptor.forClass(RemoveFromCartResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        assertTrue(resCaptor.getValue().success);
        assertEquals(0, resCaptor.getValue().totalItemsInCart);
    }
}
