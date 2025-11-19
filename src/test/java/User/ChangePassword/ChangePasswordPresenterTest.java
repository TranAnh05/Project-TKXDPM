package User.ChangePassword;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageUser.ChangePassword.ChangePasswordPresenter;
import cgx.com.adapters.ManageUser.ChangePassword.ChangePasswordViewModel;
import cgx.com.usecase.ManageUser.ChangePassword.ChangePasswordResponseData;

public class ChangePasswordPresenterTest {
	private ChangePasswordPresenter presenter;
    private ChangePasswordViewModel viewModel;
    private ChangePasswordResponseData responseData;

    @BeforeEach
    void setUp() {
        viewModel = new ChangePasswordViewModel();
        presenter = new ChangePasswordPresenter(viewModel);
        responseData = new ChangePasswordResponseData();
    }

    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG
     */
    @Test
    void test_present_successCase() {
        // --- ARRANGE ---
        responseData.success = true;
        responseData.message = "Đổi mật khẩu thành công.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        ChangePasswordViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Đổi mật khẩu thành công.", resultVM.message);
    }

    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI
     */
    @Test
    void test_present_failureCase() {
        // --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Mật khẩu cũ không chính xác.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        ChangePasswordViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Mật khẩu cũ không chính xác.", resultVM.message);
    }
}
