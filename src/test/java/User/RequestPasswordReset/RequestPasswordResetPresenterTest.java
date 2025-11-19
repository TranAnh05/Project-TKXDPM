package User.RequestPasswordReset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageUser.RequestPasswordReset.RequestPasswordResetPresenter;
import cgx.com.adapters.ManageUser.RequestPasswordReset.RequestPasswordResetViewModel;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestPasswordResetResponseData;

public class RequestPasswordResetPresenterTest {
	private RequestPasswordResetPresenter presenter;
    private RequestPasswordResetViewModel viewModel;
    private RequestPasswordResetResponseData responseData;

    @BeforeEach
    void setUp() {
        viewModel = new RequestPasswordResetViewModel();
        presenter = new RequestPasswordResetPresenter(viewModel);
        responseData = new RequestPasswordResetResponseData();
    }

    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG (Thông báo chung)
     */
    @Test
    void test_present_successCase() {
        // --- ARRANGE ---
        responseData.success = true;
        responseData.message = "Nếu thông tin của bạn là chính xác...";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        RequestPasswordResetViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Nếu thông tin của bạn là chính xác...", resultVM.message);
    }

    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI (Lỗi Validation)
     */
    @Test
    void test_present_failureCase() {
        // --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Email không đúng định dạng.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        RequestPasswordResetViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Email không đúng định dạng.", resultVM.message);
    }
}
