package User.VerifyEmail;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageUser.VerifyEmail.VerifyEmailPresenter;
import cgx.com.adapters.ManageUser.VerifyEmail.VerifyEmailViewModel;
import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailResponseData;

public class VerifyEmailPresenterTest {

    private VerifyEmailPresenter presenter;
    private VerifyEmailViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new VerifyEmailViewModel();
        presenter = new VerifyEmailPresenter(viewModel);
    }

    @Test
    @DisplayName("Success: Chuyển đổi dữ liệu thành công")
    void test_present_Success() {
        // GIVEN
        VerifyEmailResponseData response = new VerifyEmailResponseData();
        response.success = true;
        response.message = "Kích hoạt thành công";

        // WHEN
        presenter.present(response);

        // THEN
        assertEquals("true", viewModel.success); // boolean true -> string "true"
        assertEquals("Kích hoạt thành công", viewModel.message);
    }

    @Test
    @DisplayName("Failure: Chuyển đổi dữ liệu lỗi")
    void test_present_Failure() {
        // GIVEN
        VerifyEmailResponseData response = new VerifyEmailResponseData();
        response.success = false;
        response.message = "Token hết hạn";

        // WHEN
        presenter.present(response);

        // THEN
        assertEquals("false", viewModel.success); // boolean false -> string "false"
        assertEquals("Token hết hạn", viewModel.message);
    }
}