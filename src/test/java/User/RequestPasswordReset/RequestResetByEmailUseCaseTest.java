package User.RequestPasswordReset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.IEmailService;
import cgx.com.usecase.Interface_Common.IPasswordHasher;
import cgx.com.usecase.Interface_Common.IPasswordResetTokenIdGenerator;
import cgx.com.usecase.Interface_Common.IPasswordResetTokenRepository;
import cgx.com.usecase.Interface_Common.ISecureTokenGenerator;
import cgx.com.usecase.Interface_Common.PasswordResetTokenData;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestPasswordResetOutputBoundary;
import cgx.com.usecase.ManageUser.RequestPasswordReset.EmailResetRequest;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestPasswordResetResponseData;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestResetByEmailUseCase;

@ExtendWith(MockitoExtension.class)
public class RequestResetByEmailUseCaseTest {

    // 1. Mock Dependencies
    @Mock private IUserRepository mockUserRepo;
    @Mock private IPasswordResetTokenRepository mockTokenRepo;
    @Mock private IPasswordResetTokenIdGenerator mockTokenIdGen;
    @Mock private ISecureTokenGenerator mockSecureTokenGen;
    @Mock private IPasswordHasher mockHasher;
    @Mock private RequestPasswordResetOutputBoundary mockOutputBoundary;
    @Mock private IEmailService mockEmailService;

    // 2. Class under test
    private RequestResetByEmailUseCase useCase;

    // 3. Test Data
    private UserData foundUser;
    
    // Thông báo chuẩn bảo mật (dùng chung cho nhiều test case)
    private static final String SECURITY_MESSAGE = 
        "Nếu thông tin của bạn là chính xác và tồn tại trong hệ thống, một hướng dẫn đặt lại mật khẩu đã được gửi.";

    @BeforeEach
    void setUp() {
        useCase = new RequestResetByEmailUseCase(
            mockUserRepo, mockTokenRepo, mockTokenIdGen, 
            mockSecureTokenGen, mockHasher, mockOutputBoundary, mockEmailService
        );

        // User giả lập tìm thấy trong DB
        foundUser = new UserData(
            "user-1", "test@mail.com", "hashed_pass", "Nguyen", "Van A", 
            null, UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now(), Instant.now()
        );
    }
    @Test
    @DisplayName("Fail: Email không đúng định dạng")
    void test_Fail_InvalidEmailFormat() {
        EmailResetRequest input = new EmailResetRequest("invalid-email");

        useCase.execute(input);

        ArgumentCaptor<RequestPasswordResetResponseData> captor = ArgumentCaptor.forClass(RequestPasswordResetResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Email không đúng định dạng"));
        
        verify(mockUserRepo, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Success (Fake): Email không tồn tại -> Vẫn báo thành công (Chống dò user)")
    void test_SuccessFake_UserNotFound() {
        EmailResetRequest input = new EmailResetRequest("unknown@mail.com");

        when(mockUserRepo.findByEmail("unknown@mail.com")).thenReturn(null);

        // WHEN
        useCase.execute(input);

        // THEN
        // 1. Verify KHÔNG lưu token, KHÔNG gửi mail
        verify(mockTokenRepo, never()).save(any());
        verify(mockEmailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());

        // 2. Verify Output: Vẫn báo thành công như bình thường
        ArgumentCaptor<RequestPasswordResetResponseData> captor = ArgumentCaptor.forClass(RequestPasswordResetResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertTrue(captor.getValue().success);
        assertEquals(SECURITY_MESSAGE, captor.getValue().message);
    }

    @Test
    @DisplayName("Success (Real): User tồn tại -> Tạo Token -> Lưu DB -> Gửi Mail")
    void test_SuccessReal_UserFound() {
        EmailResetRequest input = new EmailResetRequest("test@mail.com");

        // GIVEN:
        when(mockUserRepo.findByEmail("test@mail.com")).thenReturn(foundUser);
        
        // Mock các generator
        when(mockSecureTokenGen.generate()).thenReturn("plain-123456"); // Token gốc gửi mail
        when(mockHasher.hash("plain-123456")).thenReturn("hashed-xyz"); // Token băm lưu DB
        when(mockTokenIdGen.generate()).thenReturn("token-id-100");

        // WHEN
        useCase.execute(input);

        // THEN
        
        // 1. Verify Lưu Token đã băm vào DB
        ArgumentCaptor<PasswordResetTokenData> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenData.class);
        verify(mockTokenRepo).save(tokenCaptor.capture());
        
        PasswordResetTokenData savedToken = tokenCaptor.getValue();
        assertEquals("hashed-xyz", savedToken.hashedToken); // Phải lưu bản băm
        assertEquals("user-1", savedToken.userId);
        assertNotNull(savedToken.expiresAt);

        // 2. Verify Gửi Email chứa Token gốc
        verify(mockEmailService).sendPasswordResetEmail(
            eq("test@mail.com"), 
            eq("Nguyen Van A"), 
            eq("plain-123456") 
        );

        // 3. Verify Output
        ArgumentCaptor<RequestPasswordResetResponseData> resCaptor = ArgumentCaptor.forClass(RequestPasswordResetResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());

        assertTrue(resCaptor.getValue().success);
        assertEquals(SECURITY_MESSAGE, resCaptor.getValue().message);
    }
}