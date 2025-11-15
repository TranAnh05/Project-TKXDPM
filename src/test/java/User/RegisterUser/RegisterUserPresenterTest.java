package User.RegisterUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import adapters.ManageUser.RegisterUser.RegisterUserPresenter;
import adapters.ManageUser.RegisterUser.RegisterUserViewModel;
import usecase.ManageUser.RegisterUser.RegisterUserResponseData;

/**
 * Unit Test cho Presenter - Layer 2 (ĐÃ ĐƯỢC REFACTOR)
 *
 * Cập nhật lại test để phản ánh logic mới của Presenter:
 * - Inject ViewModel vào constructor của Presenter.
 * - Kiểm tra 'success' là String ("true" / "false").
 * - Kiểm tra DTO lồng nhau (newUser).
 */
public class RegisterUserPresenterTest {

    private RegisterUserPresenter presenter;
    private RegisterUserViewModel viewModel; // ViewModel giờ được tạo ở đây
    private RegisterUserResponseData responseData;

    @BeforeEach
    void setUp() {
        // 1. Tạo ViewModel trước
        viewModel = new RegisterUserViewModel();
        // 2. Inject ViewModel vào Presenter
        presenter = new RegisterUserPresenter(viewModel);
        
        responseData = new RegisterUserResponseData();
    }

    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG
     */
    @Test
    void test_present_successCase() {
        // --- ARRANGE ---
        responseData.success = true;
        responseData.message = "Đăng ký thành công!";
        responseData.createdUserId = "user-123";
        responseData.email = "success@example.com";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        // Lấy model từ presenter (chính là viewModel mà chúng ta đã inject)
        RegisterUserViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Đăng ký thành công!", resultVM.message);
        
        // Kiểm tra DTO con (nested DTO)
        assertNotNull(resultVM.newUser);
        assertEquals("user-123", resultVM.newUser.id);
        assertEquals("success@example.com", resultVM.newUser.email);
    }

    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI
     */
    @Test
    void test_present_failureCase() {
        // --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Email đã tồn tại.";
        // Các trường khác là null (mặc định)

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        RegisterUserViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success, "Trường 'success' nên là String 'false'");
        assertEquals("Email đã tồn tại.", resultVM.message);
        
        // Quan trọng: ViewModel không được chứa dữ liệu rác
        assertNull(resultVM.newUser);
    }
}
