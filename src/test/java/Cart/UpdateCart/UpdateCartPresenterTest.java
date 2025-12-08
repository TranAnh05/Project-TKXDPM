package Cart.UpdateCart;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.Cart.UpdateCart.UpdateCartPresenter;
import cgx.com.adapters.Cart.UpdateCart.UpdateCartViewModel;
import cgx.com.usecase.Cart.UpdateCart.UpdateCartResponseData;

public class UpdateCartPresenterTest {

    private UpdateCartPresenter presenter;
    private UpdateCartViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new UpdateCartViewModel();
        presenter = new UpdateCartPresenter(viewModel);
    }

    @Test
    @DisplayName("Success: Chuyển đổi dữ liệu thành công")
    void test_present_Success() {
        // GIVEN: UseCase trả về Success và số lượng mới
        UpdateCartResponseData response = new UpdateCartResponseData();
        response.success = true;
        response.message = "OK";
        response.totalItemsInCart = 5;

        // WHEN
        presenter.present(response);

        // THEN
        assertEquals("true", viewModel.isSuccess);
        assertEquals("OK", viewModel.message);
        assertEquals("5", viewModel.totalItemsInCart);
    }

    @Test
    @DisplayName("Failure: Chuyển đổi thông báo lỗi")
    void test_present_Failure() {
        // GIVEN: UseCase trả về Lỗi
        UpdateCartResponseData response = new UpdateCartResponseData();
        response.success = false;
        response.message = "Hết hàng";
        response.totalItemsInCart = 0; // Khi lỗi thường trả về 0 hoặc số cũ

        // WHEN
        presenter.present(response);

        // THEN
        assertEquals("false", viewModel.isSuccess);
        assertEquals("Hết hàng", viewModel.message);
        assertEquals("0", viewModel.totalItemsInCart);
    }
}
