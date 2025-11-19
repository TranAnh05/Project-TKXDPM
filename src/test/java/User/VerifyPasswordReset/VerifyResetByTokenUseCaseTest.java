package User.VerifyPasswordReset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import usecase.ManageUser.IPasswordHasher;
import usecase.ManageUser.IPasswordResetTokenRepository;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.PasswordResetTokenData;
import usecase.ManageUser.UserData;
import usecase.ManageUser.VerifyPasswordReset.VerifyPasswordResetOutputBoundary;
import usecase.ManageUser.VerifyPasswordReset.VerifyPasswordResetRequestData;
import usecase.ManageUser.VerifyPasswordReset.VerifyPasswordResetResponseData;
import usecase.ManageUser.VerifyPasswordReset.VerifyResetByTokenUseCase;

@ExtendWith(MockitoExtension.class)
public class VerifyResetByTokenUseCaseTest {
	// 1. Mock các dependencies
    @Mock private IUserRepository mockUserRepository;
    @Mock private IPasswordResetTokenRepository mockTokenRepository;
    @Mock private IPasswordHasher mockPasswordHasher;
    @Mock private VerifyPasswordResetOutputBoundary mockOutputBoundary;

    // 2. Lớp cần test
    private VerifyResetByTokenUseCase useCase;

    // 3. Dữ liệu mẫu
    private VerifyPasswordResetRequestData requestData;
    private PasswordResetTokenData validTokenData;
    private UserData validUserData;
    
    @BeforeEach
    void setUp() {
        useCase = new VerifyResetByTokenUseCase(
            mockUserRepository,
            mockTokenRepository,
            mockPasswordHasher,
            mockOutputBoundary
        );

        requestData = new VerifyPasswordResetRequestData("plain-text-token-abc", "newPassword456");

        // Token hợp lệ, chưa hết hạn
        validTokenData = new PasswordResetTokenData(
            "token-record-123",
            "hashed-token-xyz",
            "user-123",
            Instant.now().plus(1, ChronoUnit.HOURS) // Hết hạn trong 1 giờ nữa
        );
        
        validUserData = new UserData();
        validUserData.userId = "user-123";
        validUserData.hashedPassword = "hashed_oldPassword123";
    }
    
    /**
     * Test kịch bản THÀNH CÔNG
     */
    @Test
    void test_execute_success() {
        // --- ARRANGE ---
        // 1. Giả lập băm token (để tìm)
        when(mockPasswordHasher.hash("plain-text-token-abc")).thenReturn("hashed-token-xyz");
        // 2. Giả lập tìm thấy Token
        when(mockTokenRepository.findByHashedToken("hashed-token-xyz")).thenReturn(validTokenData);
        // 3. Giả lập tìm thấy User
        when(mockUserRepository.findByUserId("user-123")).thenReturn(validUserData);
        // 4. Giả lập băm mật khẩu mới
        when(mockPasswordHasher.hash("newPassword456")).thenReturn("hashed_newPassword456");

        ArgumentCaptor<VerifyPasswordResetResponseData> responseCaptor =
            ArgumentCaptor.forClass(VerifyPasswordResetResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Kiểm tra các bước
        verify(mockPasswordHasher).hash("plain-text-token-abc"); // Băm token để tìm
        verify(mockTokenRepository).findByHashedToken("hashed-token-xyz");
        verify(mockUserRepository).findByUserId("user-123");
        verify(mockPasswordHasher).hash("newPassword456"); // Băm pass mới
        verify(mockUserRepository).update(any(UserData.class)); // Đã cập nhật User
        verify(mockTokenRepository).deleteByTokenId("token-record-123"); // Đã xóa Token
        
        // 2. Kiểm tra response
        verify(mockOutputBoundary).present(responseCaptor.capture());
        VerifyPasswordResetResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertEquals("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập ngay bây giờ.", presentedResponse.message);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Token không tìm thấy
     */
    @Test
    void test_execute_failure_tokenNotFound() {
        // --- ARRANGE ---
        when(mockPasswordHasher.hash("plain-text-token-abc")).thenReturn("hashed-token-xyz");
        // 1. Giả lập KHÔNG tìm thấy Token
        when(mockTokenRepository.findByHashedToken("hashed-token-xyz")).thenReturn(null);
        
        ArgumentCaptor<VerifyPasswordResetResponseData> responseCaptor =
            ArgumentCaptor.forClass(VerifyPasswordResetResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        VerifyPasswordResetResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Token không hợp lệ hoặc đã hết hạn.", presentedResponse.message);
        
        // Quan trọng: Không bao giờ gọi CSDL User
        verify(mockUserRepository, never()).findByUserId(anyString());
        verify(mockUserRepository, never()).update(any(UserData.class));
    }
    
    /**
     * Test kịch bản THẤT BẠI: Token đã hết hạn
     */
    @Test
    void test_execute_failure_tokenExpired() {
        // --- ARRANGE ---
        // 1. Tạo token đã hết hạn
        PasswordResetTokenData expiredTokenData = new PasswordResetTokenData(
            "token-record-123", "hashed-token-xyz", "user-123",
            Instant.now().minus(1, ChronoUnit.MINUTES) // Đã hết hạn 1 phút trước
        );
        
        when(mockPasswordHasher.hash("plain-text-token-abc")).thenReturn("hashed-token-xyz");
        when(mockTokenRepository.findByHashedToken("hashed-token-xyz")).thenReturn(expiredTokenData);
        
        ArgumentCaptor<VerifyPasswordResetResponseData> responseCaptor =
            ArgumentCaptor.forClass(VerifyPasswordResetResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        VerifyPasswordResetResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Token không hợp lệ hoặc đã hết hạn.", presentedResponse.message);
        
        verify(mockUserRepository, never()).update(any(UserData.class));
    }
    
    /**
     * Test kịch bản THẤT BẠI: Mật khẩu mới quá yếu (Lỗi Validation)
     */
    @Test
    void test_execute_failure_newPasswordTooShort() {
        // --- ARRANGE ---
        VerifyPasswordResetRequestData badRequest = 
            new VerifyPasswordResetRequestData("plain-text-token-abc", "123"); // Pass mới "123"

        ArgumentCaptor<VerifyPasswordResetResponseData> responseCaptor =
            ArgumentCaptor.forClass(VerifyPasswordResetResponseData.class);

        // --- ACT ---
        useCase.execute(badRequest);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        VerifyPasswordResetResponseData presentedResponse = responseCaptor.getValue();
        
        assertFalse(presentedResponse.success);
        assertEquals("Mật khẩu phải có ít nhất 8 ký tự.", presentedResponse.message);
        
        // Dừng ngay, không gọi CSDL
        verify(mockTokenRepository, never()).findByHashedToken(anyString());
    }
    
    /**
     * Test kịch bản THẤT BẠI: Lỗi hệ thống (ví dụ: CSDL sập khi XÓA token)
     * (Đây vẫn là kịch bản THÀNH CÔNG đối với người dùng)
     */
    @Test
    void test_execute_success_systemErrorOnTokenDelete() {
        // --- ARRANGE ---
        // 1. Mọi thứ đều OK
        when(mockPasswordHasher.hash("plain-text-token-abc")).thenReturn("hashed-token-xyz");
        when(mockTokenRepository.findByHashedToken("hashed-token-xyz")).thenReturn(validTokenData);
        when(mockUserRepository.findByUserId("user-123")).thenReturn(validUserData);
        when(mockPasswordHasher.hash("newPassword456")).thenReturn("hashed_newPassword456");
        
        // 2. Giả lập CSDL sập khi XÓA token
        doThrow(new RuntimeException("Database connection failed on delete"))
            .when(mockTokenRepository)
            .deleteByTokenId("token-record-123");

        ArgumentCaptor<VerifyPasswordResetResponseData> responseCaptor =
            ArgumentCaptor.forClass(VerifyPasswordResetResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Kiểm tra: User VẪN được cập nhật
        verify(mockUserRepository).update(any(UserData.class));
        // 2. Kiểm tra: ĐÃ cố gắng xóa token
        verify(mockTokenRepository).deleteByTokenId("token-record-123");
        
        // 3. Kiểm tra response (VẪN LÀ THÀNH CÔNG)
        verify(mockOutputBoundary).present(responseCaptor.capture());
        VerifyPasswordResetResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertEquals("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập ngay bây giờ.", presentedResponse.message);
    }
}
