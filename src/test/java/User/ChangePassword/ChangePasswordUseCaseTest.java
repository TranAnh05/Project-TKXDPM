package User.ChangePassword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Entities.UserRole;
import usecase.ManageUser.IAuthTokenValidator;
import usecase.ManageUser.IPasswordHasher;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;
import usecase.ManageUser.ChangePassword.ChangePasswordOutputBoundary;
import usecase.ManageUser.ChangePassword.ChangePasswordRequestData;
import usecase.ManageUser.ChangePassword.ChangePasswordResponseData;
import usecase.ManageUser.ChangePassword.ChangePasswordUseCase;
import usecase.ManageUser.ViewUserProfile.AuthPrincipal;

@ExtendWith(MockitoExtension.class)
public class ChangePasswordUseCaseTest {
	// 1. Mock các dependencies
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IUserRepository mockUserRepository;
    @Mock private IPasswordHasher mockPasswordHasher;
    @Mock private ChangePasswordOutputBoundary mockOutputBoundary;

    // 2. Lớp cần test
    private ChangePasswordUseCase useCase;

    // 3. Dữ liệu mẫu
    private ChangePasswordRequestData requestData;
    private AuthPrincipal validPrincipal;
    private UserData existingUserData;
    
    
    @BeforeEach
    void setUp() {
        useCase = new ChangePasswordUseCase(
            mockTokenValidator,
            mockUserRepository,
            mockPasswordHasher,
            mockOutputBoundary
        );

        // Dữ liệu MỚI người dùng gửi lên
        requestData = new ChangePasswordRequestData(
            "Bearer jwt.token.string",
            "oldPassword123", // Mật khẩu cũ
            "newPassword456"  // Mật khẩu mới
        );
        
        validPrincipal = new AuthPrincipal("user-123", "test@example.com", UserRole.CUSTOMER);

        // Dữ liệu CŨ đang trong CSDL
        existingUserData = new UserData();
        existingUserData.userId = "user-123";
        existingUserData.hashedPassword = "hashed_oldPassword123"; // Pass cũ đã băm
    }
    
    /**
     * Test kịch bản THÀNH CÔNG
     */
    @Test
    void test_execute_success() {
        // --- ARRANGE ---
        // 1. Giả lập Token hợp lệ
        when(mockTokenValidator.validate("Bearer jwt.token.string")).thenReturn(validPrincipal);
        // 2. Giả lập tìm thấy User
        when(mockUserRepository.findByUserId("user-123")).thenReturn(existingUserData);
        // 3. Giả lập Mật khẩu cũ ĐÚNG
        when(mockPasswordHasher.verify("oldPassword123", "hashed_oldPassword123")).thenReturn(true);
        // 4. Giả lập việc băm mật khẩu mới
        when(mockPasswordHasher.hash("newPassword456")).thenReturn("hashed_newPassword456");

        // 5. "Bắt" DTO được gửi đến CSDL
        ArgumentCaptor<UserData> userDataCaptor = ArgumentCaptor.forClass(UserData.class);
        when(mockUserRepository.update(userDataCaptor.capture())).thenReturn(null);
        
        ArgumentCaptor<ChangePasswordResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ChangePasswordResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Kiểm tra các bước
        verify(mockTokenValidator).validate("Bearer jwt.token.string");
        verify(mockUserRepository).findByUserId("user-123");
        verify(mockPasswordHasher).verify("oldPassword123", "hashed_oldPassword123"); // Đã check pass cũ
        verify(mockPasswordHasher).hash("newPassword456"); // Đã băm pass mới
        verify(mockUserRepository).update(any(UserData.class)); // Đã lưu
        
        // 2. Kiểm tra dữ liệu GỬI ĐẾN CSDL
        UserData capturedData = userDataCaptor.getValue();
        assertEquals("hashed_newPassword456", capturedData.hashedPassword);
        assertNotNull(capturedData.updatedAt);
        
        // 3. Kiểm tra dữ liệu GỬI ĐẾN PRESENTER
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ChangePasswordResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertEquals("Đổi mật khẩu thành công.", presentedResponse.message);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Mật khẩu cũ không chính xác
     */
    @Test
    void test_execute_failure_wrongOldPassword() {
        // --- ARRANGE ---
        when(mockTokenValidator.validate("Bearer jwt.token.string")).thenReturn(validPrincipal);
        when(mockUserRepository.findByUserId("user-123")).thenReturn(existingUserData);
        // 1. Giả lập Mật khẩu cũ SAI
        when(mockPasswordHasher.verify("oldPassword123", "hashed_oldPassword123")).thenReturn(false);

        ArgumentCaptor<ChangePasswordResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ChangePasswordResponseData.class);
            
        // --- ACT ---
        useCase.execute(requestData);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ChangePasswordResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Mật khẩu cũ không chính xác.", presentedResponse.message);
        
        // Quan trọng: Không bao giờ băm pass mới hoặc lưu CSDL
        verify(mockPasswordHasher, never()).hash(anyString());
        verify(mockUserRepository, never()).update(any(UserData.class));
    }
    /**
     * Test kịch bản THẤT BẠI: Mật khẩu mới quá yếu (Lỗi Validation)
     */
    @Test
    void test_execute_failure_newPasswordTooShort() {
        // --- ARRANGE ---
        ChangePasswordRequestData badRequest = new ChangePasswordRequestData(
            "Bearer jwt.token.string",
            "oldPassword123",
            "123" // Mật khẩu mới quá ngắn
        );
        
        when(mockTokenValidator.validate("Bearer jwt.token.string")).thenReturn(validPrincipal);
        when(mockUserRepository.findByUserId("user-123")).thenReturn(existingUserData);
        // 1. Mật khẩu cũ vẫn ĐÚNG
        when(mockPasswordHasher.verify("oldPassword123", "hashed_oldPassword123")).thenReturn(true);

        ArgumentCaptor<ChangePasswordResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ChangePasswordResponseData.class);

        // --- ACT ---
        useCase.execute(badRequest);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ChangePasswordResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Mật khẩu phải có ít nhất 8 ký tự.", presentedResponse.message);
        
        verify(mockUserRepository, never()).update(any(UserData.class));
    }
    
    /**
     * Test kịch bản THẤT BẠI: Mật khẩu mới trùng mật khẩu cũ
     */
    @Test
    void test_execute_failure_newPasswordSameAsOld() {
        // --- ARRANGE ---
        ChangePasswordRequestData badRequest = new ChangePasswordRequestData(
            "Bearer jwt.token.string",
            "oldPassword123",
            "oldPassword123" // Mật khẩu mới trùng cũ
        );
        
        when(mockTokenValidator.validate("Bearer jwt.token.string")).thenReturn(validPrincipal);
        when(mockUserRepository.findByUserId("user-123")).thenReturn(existingUserData);
        when(mockPasswordHasher.verify("oldPassword123", "hashed_oldPassword123")).thenReturn(true);

        ArgumentCaptor<ChangePasswordResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ChangePasswordResponseData.class);

        // --- ACT ---
        useCase.execute(badRequest);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ChangePasswordResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Mật khẩu mới không được trùng với mật khẩu cũ.", presentedResponse.message);
        
        verify(mockUserRepository, never()).update(any(UserData.class));
    }
    
    /*
     * Test kịch bản THẤT BẠI: csdl sập
     * */
    @Test
    void test_execute_databaseFailure() {
        // --- ARRANGE ---
        // 1. Token hợp lệ
        when(mockTokenValidator.validate("Bearer jwt.token.string"))
                .thenReturn(validPrincipal);

        // 2. User tồn tại
        when(mockUserRepository.findByUserId("user-123"))
                .thenReturn(existingUserData);

        // 3. Mật khẩu cũ đúng
        when(mockPasswordHasher.verify("oldPassword123", "hashed_oldPassword123"))
                .thenReturn(true);

        // 4. Băm mật khẩu mới
        when(mockPasswordHasher.hash("newPassword456"))
                .thenReturn("hashed_newPassword456");

        // 5. Database **ném exception**
        when(mockUserRepository.update(any(UserData.class)))
                .thenThrow(new RuntimeException("DB connection lost"));

        ArgumentCaptor<ChangePasswordResponseData> responseCaptor =
                ArgumentCaptor.forClass(ChangePasswordResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // Xác minh các bước trước khi lỗi DB xảy ra
        verify(mockTokenValidator).validate("Bearer jwt.token.string");
        verify(mockUserRepository).findByUserId("user-123");
        verify(mockPasswordHasher).verify("oldPassword123", "hashed_oldPassword123");
        verify(mockPasswordHasher).hash("newPassword456");

        // Xác minh update() được gọi và **lỗi xảy ra**
        verify(mockUserRepository).update(any(UserData.class));

        // Kiểm tra dữ liệu gửi về Presenter
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ChangePasswordResponseData response = responseCaptor.getValue();

        assertFalse(response.success);
        assertEquals("Đã xảy ra lỗi hệ thống không xác định.", response.message);
    }
}
