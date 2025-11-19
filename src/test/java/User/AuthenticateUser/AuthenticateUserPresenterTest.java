package User.AuthenticateUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.Entities.UserRole;
import cgx.com.adapters.ManageUser.AuthencicateUser.AuthenticateUserPresenter;
import cgx.com.adapters.ManageUser.AuthencicateUser.AuthenticateUserViewModel;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserResponseData;

public class AuthenticateUserPresenterTest {
	private AuthenticateUserPresenter presenter;
    private AuthenticateUserViewModel viewModel;
    private AuthenticateUserResponseData responseData;

    @BeforeEach
    void setUp() {
        viewModel = new AuthenticateUserViewModel();
        presenter = new AuthenticateUserPresenter(viewModel);
        responseData = new AuthenticateUserResponseData();
    }

    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG
     */
    @Test
    void test_present_successCase() {
        // --- ARRANGE ---
        responseData.success = true;
        responseData.message = "Đăng nhập thành công!";
        responseData.token = "jwt.token.string";
        responseData.userId = "user-123";
        responseData.email = "customer@example.com";
        responseData.role = UserRole.CUSTOMER; // <-- Dùng Enum

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        AuthenticateUserViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Đăng nhập thành công!", resultVM.message);
        
        assertNotNull(resultVM.loggedInUser);
        assertEquals("jwt.token.string", resultVM.loggedInUser.token);
        assertEquals("user-123", resultVM.loggedInUser.userId);
        assertEquals("customer@example.com", resultVM.loggedInUser.email);
        assertEquals("CUSTOMER", resultVM.loggedInUser.role); // <-- Kiểm tra đã chuyển thành String
    }

    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI
     */
    @Test
    void test_present_failureCase() {
        // --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Sai thông tin đăng nhập.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        AuthenticateUserViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Sai thông tin đăng nhập.", resultVM.message);
        assertNull(resultVM.loggedInUser, "loggedInUser DTO nên là null khi thất bại");
    }
}
