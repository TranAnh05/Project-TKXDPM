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
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

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

    @Test
    @DisplayName("KB1: (Token null)")
    void test_KB1_Fail_NoAuth() {
        ViewCartRequestData input = new ViewCartRequestData(null);
        
        when(mockTokenValidator.validate(null)).thenThrow(new SecurityException("Token lỗi"));

        useCase.execute(input);

        ArgumentCaptor<ViewCartResponseData> captor = ArgumentCaptor.forClass(ViewCartResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Token lỗi"));
    }

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
}