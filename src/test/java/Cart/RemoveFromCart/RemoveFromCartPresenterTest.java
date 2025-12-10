package Cart.RemoveFromCart;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.Cart.RemoveFromCart.RemoveFromCartPresenter;
import cgx.com.adapters.Cart.RemoveFromCart.RemoveFromCartViewModel;
import cgx.com.usecase.Cart.RemoveFromCart.RemoveFromCartResponseData;

public class RemoveFromCartPresenterTest {

    private RemoveFromCartPresenter presenter;
    private RemoveFromCartViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new RemoveFromCartViewModel();
        presenter = new RemoveFromCartPresenter(viewModel);
    }

    @Test
    @DisplayName("Failure: Chuyển đổi dữ liệu lỗi")
    void test_present_Failure() {
        // GIVEN
        RemoveFromCartResponseData response = new RemoveFromCartResponseData();
        response.success = false;
        response.message = "Giỏ hàng lỗi";
        response.totalItemsInCart = 5; // Giữ nguyên số cũ nếu lỗi

        // WHEN
        presenter.present(response);

        // THEN
        assertEquals("false", viewModel.isSuccess);
        assertEquals("Giỏ hàng lỗi", viewModel.message);
        assertEquals("5", viewModel.totalItemsInCart);
    }

    @Test
    @DisplayName("Success: Chuyển đổi dữ liệu thành công")
    void test_present_Success() {
        // GIVEN
        RemoveFromCartResponseData response = new RemoveFromCartResponseData();
        response.success = true;
        response.message = "Đã xóa";
        response.totalItemsInCart = 0;

        // WHEN
        presenter.present(response);

        // THEN
        assertEquals("true", viewModel.isSuccess);
        assertEquals("Đã xóa", viewModel.message);
        assertEquals("0", viewModel.totalItemsInCart);
    }
}