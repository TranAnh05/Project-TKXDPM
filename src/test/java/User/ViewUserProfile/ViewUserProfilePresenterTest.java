package User.ViewUserProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageUser.ViewUserProfile.ViewUserProfilePresenter;
import cgx.com.adapters.ManageUser.ViewUserProfile.ViewUserProfileViewModel;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

public class ViewUserProfilePresenterTest {
	private ViewUserProfilePresenter presenter;
    private ViewUserProfileViewModel viewModel;
    private ViewUserProfileResponseData responseData;

    @BeforeEach
    void setUp() {
        viewModel = new ViewUserProfileViewModel();
        presenter = new ViewUserProfilePresenter(viewModel);
        responseData = new ViewUserProfileResponseData();
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG (có số điện thoại)
     */
    @Test
    void test_present_successCase_withPhone() {
        // --- ARRANGE ---
        responseData.success = true;
        responseData.message = "Lấy thông tin thành công.";
        responseData.userId = "user-123";
        responseData.email = "test@example.com";
        responseData.firstName = "John";
        responseData.lastName = "Doe";
        responseData.phoneNumber = "0909123456";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        ViewUserProfileViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Lấy thông tin thành công.", resultVM.message);
        
        assertNotNull(resultVM.userProfile);
        assertEquals("user-123", resultVM.userProfile.id);
        assertEquals("test@example.com", resultVM.userProfile.email);
        assertEquals("John", resultVM.userProfile.firstName);
        assertEquals("Doe", resultVM.userProfile.lastName);
        assertEquals("0909123456", resultVM.userProfile.phoneNumber);
    }
    

    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI
     */
    @Test
    void test_present_failureCase() {
        // --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Token đã hết hạn.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        ViewUserProfileViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Token đã hết hạn.", resultVM.message);
        assertNull(resultVM.userProfile, "userProfile DTO nên là null khi thất bại");
    }
}
