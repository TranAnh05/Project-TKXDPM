package User.UpdateUserProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import adapters.ManageUser.UpdateUserProfile.UpdateUserProfilePresenter;
import adapters.ManageUser.UpdateUserProfile.UpdateUserProfileViewModel;
import usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

public class UpdateUserProfilePresenterTest {
	private UpdateUserProfilePresenter presenter;
    private UpdateUserProfileViewModel viewModel;
    private ViewUserProfileResponseData responseData;

    @BeforeEach
    void setUp() {
        viewModel = new UpdateUserProfileViewModel();
        presenter = new UpdateUserProfilePresenter(viewModel);
        responseData = new ViewUserProfileResponseData();
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG
     */
    @Test
    void test_present_successCase() {
        // --- ARRANGE ---
        responseData.success = true;
        responseData.message = "Cập nhật hồ sơ thành công.";
        responseData.userId = "user-123";
        responseData.email = "test@example.com";
        responseData.firstName = "Jane"; // Tên mới
        responseData.lastName = "Doe-Smith"; // Họ mới
        responseData.phoneNumber = "0909888777"; // SĐT mới

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        UpdateUserProfileViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Cập nhật hồ sơ thành công.", resultVM.message);
        
        assertNotNull(resultVM.updatedProfile);
        assertEquals("user-123", resultVM.updatedProfile.id);
        assertEquals("Jane", resultVM.updatedProfile.firstName);
        assertEquals("0909888777", resultVM.updatedProfile.phoneNumber);
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI
     */
    @Test
    void test_present_failureCase() {
        // --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Tên không được để trống.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        UpdateUserProfileViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Tên không được để trống.", resultVM.message);
        assertNull(resultVM.updatedProfile, "updatedProfile DTO nên là null khi thất bại");
    }
}
