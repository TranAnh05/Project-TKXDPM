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

import cgx.com.Entities.ProductAvailability;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Cart.CartData;
import cgx.com.usecase.Cart.CartItemData;
import cgx.com.usecase.Cart.ICartRepository;
import cgx.com.usecase.Cart.AddToCart.AddToCartOutputBoundary;
import cgx.com.usecase.Cart.AddToCart.AddToCartRequestData;
import cgx.com.usecase.Cart.AddToCart.AddToCartResponseData;
import cgx.com.usecase.Cart.AddToCart.AddToCartUseCase;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

@ExtendWith(MockitoExtension.class)
public class AddToCartUseCaseTest {

    // 1. Mock Dependencies
    @Mock private ICartRepository mockCartRepo;
    @Mock private IDeviceRepository mockDeviceRepo;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private AddToCartOutputBoundary mockOutputBoundary;

    // 2. Class under test
    private AddToCartUseCase useCase;

    // 3. Test Data
    private AuthPrincipal userPrincipal;
    private DeviceData deviceData;
    private CartData existingCart;

    @BeforeEach
    void setUp() {
        useCase = new AddToCartUseCase(mockCartRepo, mockDeviceRepo, mockTokenValidator, mockOutputBoundary);
        
        userPrincipal = new AuthPrincipal("user-1", "test@mail.com", UserRole.CUSTOMER);
        
        // Mock dữ liệu sản phẩm chuẩn (Giá 10tr, Kho 10 cái, Active)
        deviceData = new DeviceData();
        deviceData.id = "lap-1";
        deviceData.price = new BigDecimal("10000000");
        deviceData.stockQuantity = 10;
        deviceData.status = String.valueOf(ProductAvailability.AVAILABLE); 

        // Mock giỏ hàng đang có sẵn 1 món
        existingCart = new CartData();
        existingCart.userId = "user-1";
        existingCart.items = new ArrayList<>();
        existingCart.items.add(new CartItemData("lap-1", 2)); // Đang có 2 cái
        existingCart.totalEstimatedPrice = new BigDecimal("20000000");
    }

    @Test
    @DisplayName("Fail: Token rỗng hoặc không hợp lệ")
    void test_Fail_Auth() {
        AddToCartRequestData input = new AddToCartRequestData(null, "lap-1", 1);
        
        // Mock Validator ném lỗi
        when(mockTokenValidator.validate(null)).thenThrow(new SecurityException("Token lỗi"));

        useCase.execute(input);

        ArgumentCaptor<AddToCartResponseData> captor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Token lỗi", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Số lượng thêm vào <= 0")
    void test_Fail_InvalidQuantity() {
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 0);
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        
        // CartItem.validateQuantity sẽ ném lỗi
        useCase.execute(input);

        ArgumentCaptor<AddToCartResponseData> captor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("lớn hơn 0"));
    }
    
    @Test
    @DisplayName("Fail: ID của sản phẩm rỗng")
    void test_fail_EmptyID() {
    	AddToCartRequestData input = new AddToCartRequestData("token", "", 1);
    	
    	when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
    	
    	useCase.execute(input);
        
        ArgumentCaptor<AddToCartResponseData> captor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("không được để trống."));
    }

    @Test
    @DisplayName("Fail: Sản phẩm không tìm thấy trong DB")
    void test_Fail_ProductNotFound() {
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-999", 1);
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("lap-999")).thenReturn(null); // Không thấy

        useCase.execute(input);

        ArgumentCaptor<AddToCartResponseData> captor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Sản phẩm không tồn tại.", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Sản phẩm ngừng kinh doanh (DISCONTINUED)")
    void test_Fail_ProductDiscontinued() {
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 1);
        deviceData.status = "DISCONTINUED"; // Đổi trạng thái

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceData);

        useCase.execute(input);

        ArgumentCaptor<AddToCartResponseData> captor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("ngừng kinh doanh"));
    }

    @Test
    @DisplayName("Fail: Sản phẩm hết hàng (Stock = 0)")
    void test_Fail_OutOfStock_Initial() {
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 1);
        deviceData.stockQuantity = 0; // Hết hàng

        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceData);

        useCase.execute(input);

        ArgumentCaptor<AddToCartResponseData> captor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("đã hết hàng"));
    }

    @Test
    @DisplayName("Fail: Cộng dồn vượt quá tồn kho (Cart + Request > Stock)")
    void test_Fail_MergeExceedsStock() {
        // GIVEN: Kho có 10. Giỏ đã có 2. Muốn thêm 9.
        // Tổng = 11 > 10 -> Lỗi
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 9);
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceData); // Stock = 10
        when(mockCartRepo.findByUserId("user-1")).thenReturn(existingCart); // Cart has 2

        // WHEN
        useCase.execute(input);

        // THEN
        ArgumentCaptor<AddToCartResponseData> captor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Lỗi từ Cart.addItem -> "Số lượng vượt quá tồn kho..."
        assertTrue(captor.getValue().message.contains("vượt quá tồn kho"));
        
        // Verify chưa gọi save
        verify(mockCartRepo, never()).save(any());
    }

//    @Test
//    @DisplayName("Fail: Lỗi DB khi save")
//    void test_Fail_SystemError() {
//        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 1);
//        
//        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
//        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceData);
//        when(mockCartRepo.findByUserId("user-1")).thenReturn(null); // Giỏ mới
//        
//        doThrow(new RuntimeException("DB Error")).when(mockCartRepo).save(any());
//
//        useCase.execute(input);
//
//        ArgumentCaptor<AddToCartResponseData> captor = ArgumentCaptor.forClass(AddToCartResponseData.class);
//        verify(mockOutputBoundary).present(captor.capture());
//        
//        assertFalse(captor.getValue().success);
//        assertTrue(captor.getValue().message.contains("Lỗi hệ thống"));
//    }

    @Test
    @DisplayName("Success: Thêm thành công (Cộng dồn vào giỏ có sẵn)")
    void test_Success_MergeItem() {
        // GIVEN: Kho 10. Giỏ có 2 (20tr). Thêm 3 (30tr).
        // Expect: Giỏ có 5, Tổng tiền 50tr.
        AddToCartRequestData input = new AddToCartRequestData("token", "lap-1", 3);
        
        when(mockTokenValidator.validate("token")).thenReturn(userPrincipal);
        when(mockDeviceRepo.findById("lap-1")).thenReturn(deviceData);
        when(mockCartRepo.findByUserId("user-1")).thenReturn(existingCart);

        // WHEN
        useCase.execute(input);

        // THEN
        // 1. Verify dữ liệu lưu xuống DB
        ArgumentCaptor<CartData> cartCaptor = ArgumentCaptor.forClass(CartData.class);
        verify(mockCartRepo).save(cartCaptor.capture());
        CartData savedCart = cartCaptor.getValue();
        
        assertEquals(1, savedCart.items.size()); // Vẫn là 1 dòng item (do cộng dồn)
        assertEquals(5, savedCart.items.get(0).quantity); // 2 + 3 = 5
        assertEquals(0, savedCart.totalEstimatedPrice.compareTo(new BigDecimal("50000000"))); // 50tr

        // 2. Verify Output
        ArgumentCaptor<AddToCartResponseData> resCaptor = ArgumentCaptor.forClass(AddToCartResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        
        assertTrue(resCaptor.getValue().success);
        assertEquals(5, resCaptor.getValue().totalItemsInCart);
        assertEquals("Đã thêm sản phẩm vào giỏ hàng.", resCaptor.getValue().message);
    }
}