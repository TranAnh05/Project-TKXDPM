package User.AdminUpdateUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.management.relation.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Entities.AccountStatus;
import Entities.UserRole;
import adapters.ManageUser.AdminUpdateUser.AdminUpdateUserPresenter;
import adapters.ManageUser.AdminUpdateUser.AdminUpdateUserViewModel;
import usecase.ManageUser.AdminUpdateUser.AdminUpdateUserResponseData;
import usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

public class AdminUpdateUserPresenterTest {
	private AdminUpdateUserPresenter presenter;
    private AdminUpdateUserViewModel viewModel;
    private AdminUpdateUserResponseData responseData;

    @BeforeEach
    void setUp() {
        viewModel = new AdminUpdateUserViewModel();
        presenter = new AdminUpdateUserPresenter(viewModel);
        responseData = new AdminUpdateUserResponseData();
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG
     */
    @Test
    void test_present_successCase() {
        // --- ARRANGE ---
        responseData.success = true;
        responseData.message = "Cập nhật người dùng thành công.";
        responseData.userId = "user-456";
        responseData.email = "new.email@example.com";
        responseData.firstName = "Jane";
        responseData.lastName = "Doe";
        responseData.phoneNumber = "0909111222";
        responseData.role = UserRole.CUSTOMER;
        responseData.status = AccountStatus.SUSPENDED;
        

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        AdminUpdateUserViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Cập nhật người dùng thành công.", resultVM.message);
        
        assertNotNull(resultVM.updatedUser);
        assertEquals("user-456", resultVM.updatedUser.id);
        assertEquals("Jane", resultVM.updatedUser.firstName);
        assertEquals("new.email@example.com", resultVM.updatedUser.email);
        assertEquals("0909111222", resultVM.updatedUser.phoneNumber);
        assertEquals("CUSTOMER", resultVM.updatedUser.role, "Role phải được chuyển thành String");
        assertEquals("SUSPENDED", resultVM.updatedUser.status, "Status phải được chuyển thành String");
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI
     */
    @Test
    void test_present_failureCase() {
    	// --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Email mới này đã tồn tại.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        AdminUpdateUserViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Email mới này đã tồn tại.", resultVM.message);
        assertNull(resultVM.updatedUser, "updatedUser DTO nên là null khi thất bại");
    }
}
