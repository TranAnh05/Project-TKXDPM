package User.VerifyPasswordReset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IPasswordResetTokenRepository;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.PasswordResetTokenData;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.VerifyPasswordReset.VerifyPasswordResetOutputBoundary;
import cgx.com.usecase.ManageUser.VerifyPasswordReset.VerifyPasswordResetRequestData;
import cgx.com.usecase.ManageUser.VerifyPasswordReset.VerifyPasswordResetResponseData;
import cgx.com.usecase.ManageUser.VerifyPasswordReset.VerifyResetByTokenUseCase;

@ExtendWith(MockitoExtension.class)
public class VerifyResetByTokenUseCaseTest {

    // 1. Mock Dependencies
    @Mock private IUserRepository mockUserRepo;
    @Mock private IPasswordResetTokenRepository mockTokenRepo;
    @Mock private IPasswordHasher mockHasher;
    @Mock private VerifyPasswordResetOutputBoundary mockOutputBoundary;

    // 2. Class under test
    private VerifyResetByTokenUseCase useCase;

    // 3. Test Data
    private UserData userData;
    private PasswordResetTokenData validTokenData;
    private VerifyPasswordResetRequestData requestData;

    @BeforeEach
    void setUp() {
        useCase = new VerifyResetByTokenUseCase(mockUserRepo, mockTokenRepo, mockHasher, mockOutputBoundary);

        // Input mặc định hợp lệ
        requestData = new VerifyPasswordResetRequestData("plain-token-123", "newPass123");

        // User Data giả lập
        userData = new UserData(
            "user-1", "test@mail.com", "old_hashed_pass", "Nguyen", "Van A", 
            "0901234567", UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now(), Instant.now()
        );

        // Token Data giả lập (Hợp lệ, chưa hết hạn)
        validTokenData = new PasswordResetTokenData(
            "token-id-1", 
            "hashed-token-123", 
            "user-1", 
            Instant.now().plus(15, ChronoUnit.MINUTES) // Còn hạn 15p
        );
    }

    @Test
    @DisplayName("Fail: Mật khẩu mới quá ngắn (< 8 ký tự)")
    void test_Fail_InvalidNewPassword() {
        VerifyPasswordResetRequestData invalidInput = new VerifyPasswordResetRequestData("token", "123");

        // WHEN
        useCase.execute(invalidInput);

        // THEN
        ArgumentCaptor<VerifyPasswordResetResponseData> captor = ArgumentCaptor.forClass(VerifyPasswordResetResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        // Lỗi từ User.validatePassword
        assertTrue(captor.getValue().message.contains("ít nhất 8 ký tự"));
    }

    @Test
    @DisplayName("Fail: Token tìm thấy nhưng đã hết hạn")
    void test_Fail_TokenExpired() {
        // GIVEN: Token đã hết hạn 5 phút trước
        PasswordResetTokenData expiredToken = new PasswordResetTokenData(
            "token-id-1", "hashed-token-123", "user-1", 
            Instant.now().minus(5, ChronoUnit.MINUTES)
        );

        when(mockHasher.hash("plain-token-123")).thenReturn("hashed-token-123");
        when(mockTokenRepo.findByHashedToken("hashed-token-123")).thenReturn(expiredToken);

        // WHEN
        useCase.execute(requestData);

        // THEN
        ArgumentCaptor<VerifyPasswordResetResponseData> captor = ArgumentCaptor.forClass(VerifyPasswordResetResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        assertEquals("Token đã hết hạn.", captor.getValue().message);
    }
    
    @Test
    @DisplayName("Success: Đổi mật khẩu thành công -> Update DB -> Xóa Token")
    void test_Success() {
        when(mockHasher.hash("plain-token-123")).thenReturn("hashed-token-123");
        when(mockTokenRepo.findByHashedToken("hashed-token-123")).thenReturn(validTokenData);
        when(mockUserRepo.findByUserId("user-1")).thenReturn(userData);
        when(mockHasher.hash("newPass123")).thenReturn("new_hashed_pass");

        // WHEN
        useCase.execute(requestData);

        // THEN
        ArgumentCaptor<UserData> userCaptor = ArgumentCaptor.forClass(UserData.class);
        verify(mockUserRepo).update(userCaptor.capture());
        UserData updatedUser = userCaptor.getValue();
        
        assertEquals("new_hashed_pass", updatedUser.hashedPassword); 
        assertNotEquals(userData.updatedAt, updatedUser.updatedAt); 

        // Verify Token cũ bị xóa
        verify(mockTokenRepo).deleteByTokenId("token-id-1");

        // Verify Output thành công
        ArgumentCaptor<VerifyPasswordResetResponseData> resCaptor = ArgumentCaptor.forClass(VerifyPasswordResetResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());

        assertTrue(resCaptor.getValue().success);
        assertTrue(resCaptor.getValue().message.contains("thành công"));
    }
}