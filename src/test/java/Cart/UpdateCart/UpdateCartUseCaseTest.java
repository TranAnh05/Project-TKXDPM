package Cart.UpdateCart;

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

import cgx.com.Entities.ProductAvailability;
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
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

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
    private CartData existingCart;
    private DeviceData deviceData;
    private UpdateCartRequestData validRequest;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCartUseCase(mockCartRepo, mockDeviceRepo, mockTokenValidator, mockOutputBoundary);
        
        userPrincipal = new AuthPrincipal("user-1", "test@mail.com", UserRole.CUSTOMER);
        
        // Request hợp lệ mặc định (Update lap-1 thành 2 cái)
        validRequest = new UpdateCartRequestData("token", "lap-1", 2);

        // Giỏ hàng giả lập: Đang có 1 món "lap-1", số lượng 1
        existingCart = new CartData();
        existingCart.userId = "user-1";
        List<CartItemData> items = new ArrayList<>();
        items.add(new CartItemData("lap-1", 1));
        existingCart.items = items;
        existingCart.totalEstimatedPrice = new BigDecimal("20000000");

        // Sản phẩm giả lập (Kho còn 10 cái, giá 20tr)
        deviceData = new DeviceData();
        deviceData.id = "lap-1";
        deviceData.price = new BigDecimal("20000000");
        deviceData.stockQuantity = 10;
        deviceData.status = String.valueOf(ProductAvailability.AVAILABLE); // Map với Enum AVAILABLE/ACTIVE
    }

    @Test
    @DisplayName("Fail: Token rỗng hoặc null")
    void test_Fail_NoAuth() {
        UpdateCartRequestData input = new UpdateCartRequestData(null, "lap-1", 2);
        
        when(mockTokenValidator.validate(null)).thenThrow(new SecurityException("Token lỗi"));
        
        useCase.execute(input);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Token lỗi", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Số lượng <= 0")
    void test_Fail_InvalidQuantity() {
        UpdateCartRequestData input = new UpdateCartRequestData("token", "lap-1", 0);
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);

        useCase.execute(input);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Lỗi từ CartItem.validateQuantity
        assertTrue(captor.getValue().message.contains("lớn hơn 0"));
    }
    @Test
    @DisplayName("Fail: Sản phẩm không tìm thấy trong DB")
    void test_Fail_ProductNotFound() {
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("lap-1")).thenReturn(null);

        useCase.execute(validRequest);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Sản phẩm không tồn tại.", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Sản phẩm có trạng thái DISCONTINUED")
    void test_Fail_ProductDiscontinued() {
        deviceData.status = "DISCONTINUED"; // Giả lập ngừng bán

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceData);

        useCase.execute(validRequest);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Lỗi từ ComputerDevice.validateStatus
        assertTrue(captor.getValue().message.contains("ngừng kinh doanh"));
    }

    @Test
    @DisplayName("Fail: Số lượng update lớn hơn tồn kho")
    void test_Fail_NotEnoughStock() {
        // Kho có 10, muốn update lên 20
        UpdateCartRequestData input = new UpdateCartRequestData("token", "lap-1", 20);
        deviceData.stockQuantity = 10;

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceData);
        when(mockCartRepo.findByUserId("user-1")).thenReturn(existingCart);

        useCase.execute(input);

        ArgumentCaptor<UpdateCartResponseData> captor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Thông báo lỗi cụ thể từ Entity
        assertTrue(captor.getValue().message.contains("Kho chỉ còn 10"));
    }

    // =========================================================================
    // 8. KỊCH BẢN THÀNH CÔNG: HAPPY PATH
    // =========================================================================
    @Test
    @DisplayName("Success: Cập nhật thành công, tính lại giá đúng")
    void test_Success() {
        // GIVEN: Giỏ đang có 1 cái (20tr). Muốn sửa thành 3 cái.
        UpdateCartRequestData input = new UpdateCartRequestData("token", "lap-1", 3);
        
        // Mock Device: Giá 20tr, Kho 100 cái (đủ)
        deviceData.price = new BigDecimal("20000000");
        deviceData.stockQuantity = 100;

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceData);
        when(mockCartRepo.findByUserId("user-1")).thenReturn(existingCart);

        // WHEN
        useCase.execute(input);

        // THEN
        ArgumentCaptor<CartData> cartCaptor = ArgumentCaptor.forClass(CartData.class);
        verify(mockCartRepo).save(cartCaptor.capture());
        
        CartData savedCart = cartCaptor.getValue();
        
        // 1. Verify số lượng đã update
        assertEquals(3, savedCart.items.get(0).quantity);
        
        // 2. Verify Giá tiền đã update
        // Logic: (Giá cũ 20tr - 20tr) + (3 * 20tr) = 60tr
        // Hoặc đơn giản: 3 * 20tr = 60tr
        assertEquals(0, savedCart.totalEstimatedPrice.compareTo(new BigDecimal("60000000")));
        
        // 3. Verify Output
        ArgumentCaptor<UpdateCartResponseData> resCaptor = ArgumentCaptor.forClass(UpdateCartResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        assertTrue(resCaptor.getValue().success);
        assertEquals(3, resCaptor.getValue().totalItemsInCart);
    }
}