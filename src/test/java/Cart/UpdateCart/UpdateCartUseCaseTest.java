package Cart.UpdateCart;

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
import cgx.com.usecase.Cart.UpdateCart.UpdateCartOutputBoundary;
import cgx.com.usecase.Cart.UpdateCart.UpdateCartRequestData;
import cgx.com.usecase.Cart.UpdateCart.UpdateCartResponseData;
import cgx.com.usecase.Cart.UpdateCart.UpdateCartUseCase;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

@ExtendWith(MockitoExtension.class)
public class UpdateCartUseCaseTest {

    // 1. Mock Dependencies
    @Mock private ICartRepository mockCartRepo;
    @Mock private IDeviceRepository mockDeviceRepo;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private UpdateCartOutputBoundary mockOutputBoundary;

    // 2. Class under test
    private UpdateCartUseCase useCase;

    // 3. Test Data
    private AuthPrincipal userPrincipal;
    private DeviceData activeDevice;
    private CartData existingCart;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCartUseCase(mockCartRepo, mockDeviceRepo, mockTokenValidator, mockOutputBoundary);
        
        userPrincipal = new AuthPrincipal("user-1", "test@mail.com", UserRole.CUSTOMER);
        
        // Setup Sản phẩm: ID="dev-1", Giá 10, Kho 10
        activeDevice = new DeviceData();
        activeDevice.id = "dev-1";
        activeDevice.status = "AVAILABLE";
        activeDevice.price = new BigDecimal("10"); 
        activeDevice.stockQuantity = 10; 

        // Setup Giỏ hàng: Đã có 2 cái "dev-1" -> Tổng tiền hiện tại = 20
        existingCart = new CartData();
        existingCart.userId = "user-1";
        existingCart.items = new ArrayList<>(Collections.singletonList(new CartItemData("dev-1", 2)));
        existingCart.totalEstimatedPrice = new BigDecimal("20");
    }

    // =========================================================================
    // KỊCH BẢN THÀNH CÔNG (HAPPY PATH)
    // =========================================================================

    @Test
    @DisplayName("Success: Cập nhật số lượng hợp lệ (Tăng từ 2 lên 5)")
    void test_Success_UpdateQuantity() {
        // GIVEN: User muốn đổi thành 5 cái (Kho có 10 -> Đủ)
        UpdateCartRequestData input = new UpdateCartRequestData("token", "dev-1", 5);

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("dev-1")).thenReturn(activeDevice);
        when(mockCartRepo.findByUserId("user-1")).thenReturn(existingCart);

        // WHEN
        useCase.execute(input);

        // THEN
        ArgumentCaptor<CartData> cartCaptor = ArgumentCaptor.forClass(CartData.class);
        ArgumentCaptor<UpdateCartResponseData> resCaptor = ArgumentCaptor.forClass(UpdateCartResponseData.class);

        // 1. Verify dữ liệu lưu xuống DB
        verify(mockCartRepo).save(cartCaptor.capture());
        CartData savedData = cartCaptor.getValue();
        
        assertEquals(1, savedData.items.size());
        assertEquals(5, savedData.items.get(0).quantity); // Đã update thành 5
        
        // 2. Verify Logic tính tiền của Entity: 
        // Cũ (20) - Item cũ (2*10) + Item mới (5*10) = 50
        assertEquals(new BigDecimal("50"), savedData.totalEstimatedPrice);

        // 3. Verify Phản hồi
        verify(mockOutputBoundary).present(resCaptor.capture());
        assertTrue(resCaptor.getValue().success);
        assertEquals(5, resCaptor.getValue().totalItemsInCart);
    }

    // =========================================================================
    // KỊCH BẢN THẤT BẠI (FAILURE SCENARIOS) - Theo tuần tự Logic
    // =========================================================================

    // 1. Lỗi Auth
    @Test
    @DisplayName("Fail: Token rỗng")
    void test_Fail_NoAuth() {
        UpdateCartRequestData input = new UpdateCartRequestData(null, "dev-1", 5);
        
        useCase.execute(input);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Vui lòng đăng nhập.", captor.getValue().message);
    }

    // 2. Lỗi Input (Số lượng <= 0)
    @Test
    @DisplayName("Fail: Số lượng update <= 0")
    void test_Fail_InvalidQuantity() {
        UpdateCartRequestData input = new UpdateCartRequestData("token", "dev-1", 0);
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);

        useCase.execute(input);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Message này throw từ UseCase (Validate Input)
        assertEquals("Số lượng phải lớn hơn 0.", captor.getValue().message);
    }

    // 3. Lỗi Sản phẩm không tồn tại
    @Test
    @DisplayName("Fail: Sản phẩm không tìm thấy trong DB")
    void test_Fail_DeviceNotFound() {
        UpdateCartRequestData input = new UpdateCartRequestData("token", "dev-999", 5);
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("dev-999")).thenReturn(null);

        useCase.execute(input);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Sản phẩm không tồn tại.", captor.getValue().message);
    }

    // 4. Lỗi Sản phẩm ngừng kinh doanh
    @Test
    @DisplayName("Fail: Sản phẩm không còn ACTIVE")
    void test_Fail_DeviceInactive() {
        UpdateCartRequestData input = new UpdateCartRequestData("token", "dev-1", 5);
        activeDevice.status = "DISCONTINUED"; // Ngừng bán
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("dev-1")).thenReturn(activeDevice);

        useCase.execute(input);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Sản phẩm này hiện đang ngừng kinh doanh.", captor.getValue().message);
    }

    // 5. Lỗi Giỏ hàng không tồn tại
    @Test
    @DisplayName("Fail: User chưa có giỏ hàng")
    void test_Fail_CartNotFound() {
        UpdateCartRequestData input = new UpdateCartRequestData("token", "dev-1", 5);
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("dev-1")).thenReturn(activeDevice);
        when(mockCartRepo.findByUserId("user-1")).thenReturn(null); // Không có giỏ

        useCase.execute(input);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Giỏ hàng không tồn tại.", captor.getValue().message);
    }

    // 6. Lỗi Entity Logic: Sản phẩm không có trong giỏ
    @Test
    @DisplayName("Fail: Update sản phẩm chưa từng thêm vào giỏ")
    void test_Fail_ItemNotInCart() {
        UpdateCartRequestData input = new UpdateCartRequestData("token", "dev-2", 5);
        
        // Mock device 2 tồn tại
        DeviceData dev2 = new DeviceData(); dev2.id = "dev-2"; dev2.status = "AVAILABLE"; dev2.stockQuantity=10;
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("dev-2")).thenReturn(dev2);
        // Giỏ hàng có thật, nhưng chỉ chứa "dev-1", không chứa "dev-2"
        when(mockCartRepo.findByUserId("user-1")).thenReturn(existingCart);

        useCase.execute(input);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Message này throw từ Entity Cart.updateItemQuantity
        assertEquals("Sản phẩm không tìm thấy trong giỏ hàng.", captor.getValue().message);
    }

    // 7. Lỗi Entity Logic: Tồn kho không đủ
    @Test
    @DisplayName("Fail: Số lượng update vượt quá tồn kho")
    void test_Fail_StockNotEnough() {
        // Kho có 10, muốn update lên 15 -> Lỗi
        UpdateCartRequestData input = new UpdateCartRequestData("token", "dev-1", 15);
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("dev-1")).thenReturn(activeDevice);
        when(mockCartRepo.findByUserId("user-1")).thenReturn(existingCart);

        useCase.execute(input);

        verify(mockCartRepo, never()).save(any()); // Không được lưu

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Message throw từ Entity Cart
        assertTrue(captor.getValue().message.contains("Kho chỉ còn 10"));
    }

    // 8. Lỗi Hệ thống
    @Test
    @DisplayName("Fail: Lỗi DB bất ngờ")
    void test_Fail_SystemError() {
        UpdateCartRequestData input = new UpdateCartRequestData("token", "dev-1", 5);
        
        when(mockTokenValidator.validate("token")).thenThrow(new RuntimeException("DB Connection Timeout"));

        useCase.execute(input);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Lỗi hệ thống"));
    }
}
