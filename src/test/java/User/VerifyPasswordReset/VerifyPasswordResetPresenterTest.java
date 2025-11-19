package User.VerifyPasswordReset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageUser.VerifyPasswordReset.VerifyPasswordResetPresenter;
import cgx.com.adapters.ManageUser.VerifyPasswordReset.VerifyPasswordResetViewModel;
import cgx.com.usecase.ManageUser.VerifyPasswordReset.VerifyPasswordResetResponseData;

public class VerifyPasswordResetPresenterTest {
	private VerifyPasswordResetPresenter presenter;
    private VerifyPasswordResetViewModel viewModel;
    private VerifyPasswordResetResponseData responseData;

    @BeforeEach
    void setUp() {
        viewModel = new VerifyPasswordResetViewModel();
        presenter = new VerifyPasswordResetPresenter(viewModel);
        responseData = new VerifyPasswordResetResponseData();
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG
     */
    @Test
    void test_present_successCase() {
        // --- ARRANGE ---
        responseData.success = true;
        responseData.message = "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập ngay bây giờ.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        VerifyPasswordResetViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập ngay bây giờ.", resultVM.message);
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI
     */
    @Test
    void test_present_failureCase() {
        // --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Token không hợp lệ hoặc đã hết hạn.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        VerifyPasswordResetViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Token không hợp lệ hoặc đã hết hạn.", resultVM.message);
    }
}
