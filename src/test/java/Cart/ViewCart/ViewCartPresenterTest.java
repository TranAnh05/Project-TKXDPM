package Cart.ViewCart;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cgx.com.Entities.ProductAvailability;
import cgx.com.adapters.Cart.ViewCart.CartItemViewModel;
import cgx.com.adapters.Cart.ViewCart.ViewCartPresenter;
import cgx.com.adapters.Cart.ViewCart.ViewCartViewModel;
import cgx.com.usecase.Cart.ViewCart.ViewCartItemData;
import cgx.com.usecase.Cart.ViewCart.ViewCartResponseData;

import java.math.BigDecimal;
import java.util.Collections;

public class ViewCartPresenterTest {

    private ViewCartPresenter presenter;
    private ViewCartViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new ViewCartViewModel();
        presenter = new ViewCartPresenter(viewModel);
    }

    // =========================================================================
    // CASE 1: FORMAT TIỀN TỆ & TRẠNG THÁI "CÒN HÀNG"
    // =========================================================================
    @Test
    @DisplayName("Present: Format tiền đúng (10.000 đ) và Status xanh")
    void test_present_FormatMoney_Available() {
        // GIVEN
        ViewCartResponseData response = new ViewCartResponseData();
        response.success = true;
        response.totalCartPrice = new BigDecimal("50000"); // 50k
        
        ViewCartItemData item = new ViewCartItemData();
        item.currentPrice = new BigDecimal("25000");
        item.subTotal = new BigDecimal("50000");
        item.quantity = 2;
        item.availabilityStatus = ProductAvailability.AVAILABLE;
        
        response.items = Collections.singletonList(item);

        // WHEN
        presenter.present(response);

        // THEN
        assertEquals("true", viewModel.isSuccess);
        // Kiểm tra format tiền (Lưu ý: Format có thể khác nhau tùy Locale máy, ở đây giả định Locale VN)
        // Cách viết test an toàn là check xem có chứa số và ký tự mong muốn không
        assertTrue(viewModel.totalCartPrice.contains("50.000")); 
        
        CartItemViewModel itemVM = viewModel.items.get(0);
        assertTrue(itemVM.unitPrice.contains("25.000"));
        
        // Kiểm tra mapping Status
        assertEquals("Còn hàng", itemVM.statusLabel);
        assertEquals("green", itemVM.statusColor);
        assertEquals("true", itemVM.isBuyable);
    }

    // =========================================================================
    // CASE 2: TRẠNG THÁI "HẾT HÀNG"
    // =========================================================================
    @Test
    @DisplayName("Present: Mapping trạng thái Hết hàng (Đỏ, Disable)")
    void test_present_Status_OutOfStock() {
    	ViewCartResponseData response = new ViewCartResponseData();
        response.success = true;
        response.totalCartPrice = BigDecimal.ZERO; // Fix null safety
        
        ViewCartItemData item = new ViewCartItemData();
        item.availabilityStatus = ProductAvailability.OUT_OF_STOCK;
        
        // --- FIX LỖI Ở ĐÂY: Phải gán giá trị tiền để không bị NullPointer khi format ---
        item.currentPrice = BigDecimal.ZERO; 
        item.subTotal = BigDecimal.ZERO;
        item.quantity = 1;
        // --------------------------------------------------------------------------
        
        response.items = Collections.singletonList(item);

        // WHEN
        presenter.present(response);

        // THEN
        CartItemViewModel itemVM = viewModel.items.get(0);
        assertEquals("Tạm hết hàng", itemVM.statusLabel);
        assertEquals("red", itemVM.statusColor);
        assertEquals("false", itemVM.isBuyable);
        assertEquals("0 đ", itemVM.unitPrice); // Kiểm tra xem format có chạy không
    }

    // =========================================================================
    // CASE 3: XỬ LÝ LỖI (FAILURE)
    // =========================================================================
    @Test
    @DisplayName("Present: Xử lý khi Use Case thất bại")
    void test_present_Failure() {
        ViewCartResponseData response = new ViewCartResponseData();
        response.success = false;
        response.message = "Lỗi DB";
        response.totalCartPrice = null; // Giả sử null

        // WHEN
        presenter.present(response);

        // THEN
        assertEquals("false", viewModel.isSuccess);
        assertEquals("Lỗi DB", viewModel.message);
        assertEquals("0 đ", viewModel.totalCartPrice); // Fallback về 0 an toàn
        assertNotNull(viewModel.items);
        assertTrue(viewModel.items.isEmpty()); // List rỗng, không null
    }
}