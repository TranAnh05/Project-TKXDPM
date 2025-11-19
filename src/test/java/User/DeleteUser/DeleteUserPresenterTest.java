package User.DeleteUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import adapters.ManageUser.DeleteUser.DeleteUserPresenter;
import adapters.ManageUser.DeleteUser.DeleteUserViewModel;
import usecase.ManageUser.DeleteUser.DeleteUserResponseData;

public class DeleteUserPresenterTest {
	private DeleteUserPresenter presenter;
    private DeleteUserViewModel viewModel;
    private DeleteUserResponseData responseData;

    @BeforeEach
    void setUp() {
        viewModel = new DeleteUserViewModel();
        presenter = new DeleteUserPresenter(viewModel);
        responseData = new DeleteUserResponseData();
    }

    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG
     */
    @Test
    void test_present_successCase() {
        // --- ARRANGE ---
        responseData.success = true;
        responseData.message = "Xóa người dùng thành công.";
        responseData.deletedUserId = "user-456";
        responseData.newStatus = "DELETED";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        DeleteUserViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Xóa người dùng thành công.", resultVM.message);
        
        assertNotNull(resultVM.deletedUser);
        assertEquals("user-456", resultVM.deletedUser.id);
        assertEquals("DELETED", resultVM.deletedUser.status);
    }

    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI
     */
    @Test
    void test_present_failureCase() {
        // --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Admin không thể tự xóa chính mình.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        DeleteUserViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Admin không thể tự xóa chính mình.", resultVM.message);
        assertNull(resultVM.deletedUser, "deletedUser DTO nên là null khi thất bại");
    }
}
