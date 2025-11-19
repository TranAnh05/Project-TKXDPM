package User.AdminViewUserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;
import cgx.com.adapters.ManageUser.AdminViewUserDetails.AdminViewUserDetailsPresenter;
import cgx.com.adapters.ManageUser.AdminViewUserDetails.AdminViewUserDetailsViewModel;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserResponseData;

public class AdminViewUserDetailsPresenterTest {
	private AdminViewUserDetailsPresenter presenter;
    private AdminViewUserDetailsViewModel viewModel;
    private AdminUpdateUserResponseData responseData;

    @BeforeEach
    void setUp() {
        viewModel = new AdminViewUserDetailsViewModel();
        presenter = new AdminViewUserDetailsPresenter(viewModel);
        responseData = new AdminUpdateUserResponseData();
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG
     */
    @Test
    void test_present_successCase() {
        // --- ARRANGE ---
        responseData.success = true;
        responseData.message = "Lấy thông tin thành công.";
        responseData.userId = "user-456";
        responseData.email = "target@example.com";
        responseData.firstName = "Target";
        responseData.lastName = "User";
        responseData.phoneNumber = "0909123456";
        responseData.role = UserRole.CUSTOMER;
        responseData.status = AccountStatus.SUSPENDED;

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        AdminViewUserDetailsViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Lấy thông tin thành công.", resultVM.message);
        
        assertNotNull(resultVM.userProfile);
        assertEquals("user-456", resultVM.userProfile.id);
        assertEquals("target@example.com", resultVM.userProfile.email);
        assertEquals("Target", resultVM.userProfile.firstName);
        assertEquals("User", resultVM.userProfile.lastName);
        assertEquals("0909123456", resultVM.userProfile.phoneNumber);
        assertEquals("CUSTOMER", resultVM.userProfile.role);
        assertEquals("SUSPENDED", resultVM.userProfile.status);
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI
     */
    @Test
    void test_present_failureCase() {
        // --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Không có quyền truy cập.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        AdminViewUserDetailsViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Không có quyền truy cập.", resultVM.message);
        assertNull(resultVM.userProfile, "userProfile DTO nên là null khi thất bại");
    }
}
