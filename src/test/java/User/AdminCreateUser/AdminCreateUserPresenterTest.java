package User.AdminCreateUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Entities.AccountStatus;
import Entities.UserRole;
import adapters.ManageUser.AdminCreatedUser.AdminCreateUserPresenter;
import adapters.ManageUser.AdminCreatedUser.AdminCreateUserViewModel;
import usecase.ManageUser.AdminCreateNewUser.AdminCreateUserResponseData;

public class AdminCreateUserPresenterTest {
	private AdminCreateUserPresenter presenter;
    private AdminCreateUserViewModel viewModel;
    private AdminCreateUserResponseData responseData;

    @BeforeEach
    void setUp() {
        viewModel = new AdminCreateUserViewModel();
        presenter = new AdminCreateUserPresenter(viewModel);
        responseData = new AdminCreateUserResponseData();
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG
     */
    @Test
    void test_present_successCase() {
        // --- ARRANGE ---
        responseData.success = true;
        responseData.message = "Tạo tài khoản thành công!";
        responseData.createdUserId = "user-456";
        responseData.email = "new.admin@example.com";
        responseData.fullName = "New Admin";
        responseData.role = UserRole.ADMIN;
        responseData.status = AccountStatus.ACTIVE;

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        AdminCreateUserViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Tạo tài khoản thành công!", resultVM.message);
        
        assertNotNull(resultVM.createdUser);
        assertEquals("user-456", resultVM.createdUser.id);
        assertEquals("new.admin@example.com", resultVM.createdUser.email);
        assertEquals("New Admin", resultVM.createdUser.fullName);
        assertEquals("ADMIN", resultVM.createdUser.role);
        assertEquals("ACTIVE", resultVM.createdUser.status);
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI
     */
    @Test
    void test_present_failureCase() {
        // --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Email này đã tồn tại.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        AdminCreateUserViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Email này đã tồn tại.", resultVM.message);
        assertNull(resultVM.createdUser, "createdUser DTO nên là null khi thất bại");
    }
}
