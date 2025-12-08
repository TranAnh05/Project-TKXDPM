package Cart.AddToCart;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.Cart.AddToCart.AddToCartPresenter;
import cgx.com.adapters.Cart.AddToCart.AddToCartViewModel;
import cgx.com.usecase.Cart.AddToCart.AddToCartResponseData;

public class AddToCartPresenterTest {

    private AddToCartPresenter presenter;
    private AddToCartViewModel viewModel;

    @BeforeEach
    void setUp() {
        // ViewModel là POJO, khởi tạo trực tiếp
        viewModel = new AddToCartViewModel();
        presenter = new AddToCartPresenter(viewModel);
    }

    @Test
    @DisplayName("Present Success: Chuyển đổi dữ liệu thành công sang String")
    void test_present_success() {
        // GIVEN: Dữ liệu thô từ UseCase (boolean, int)
        AddToCartResponseData response = new AddToCartResponseData();
        response.success = true;
        response.message = "OK";
        response.totalItemsInCart = 5;

        // WHEN
        presenter.present(response);

        // THEN: Verify ViewModel chứa String đúng định dạng
        assertEquals("true", viewModel.success);
        assertEquals("OK", viewModel.message);
        assertEquals("5", viewModel.totalItemsInCart); // Int -> String
    }

    @Test
    @DisplayName("Present Failure: Chuyển đổi thông báo lỗi")
    void test_present_failure() {
        // GIVEN
        AddToCartResponseData response = new AddToCartResponseData();
        response.success = false;
        response.message = "Hết hàng";
        response.totalItemsInCart = 0;

        // WHEN
        presenter.present(response);

        // THEN
        assertEquals("false", viewModel.success);
        assertEquals("Hết hàng", viewModel.message);
        assertEquals("0", viewModel.totalItemsInCart);
    }
}