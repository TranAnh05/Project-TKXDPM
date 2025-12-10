package User.VerifyEmail;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.IVerificationTokenRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.VerificationTokenData;
import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailOutputBoundary;
import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailRequestData;
import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailResponseData;
import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailUseCase;

@ExtendWith(MockitoExtension.class)
public class VerifyEmailUseCaseTest {

    // 1. Mock Dependencies
    @Mock private IUserRepository mockUserRepo;
    @Mock private IVerificationTokenRepository mockTokenRepo;
    @Mock private VerifyEmailOutputBoundary mockOutputBoundary;

    // 2. Class under test
    private VerifyEmailUseCase useCase;

    // 3. Test Data
    private UserData pendingUser;
    private VerificationTokenData validTokenData;

    @BeforeEach
    void setUp() {
        useCase = new VerifyEmailUseCase(mockUserRepo, mockTokenRepo, mockOutputBoundary);

        // User giả lập (Trạng thái PENDING)
        pendingUser = new UserData(
            "user-1", "test@mail.com", "hash", "A", "B", null, 
            UserRole.CUSTOMER, AccountStatus.PENDING_VERIFICATION, Instant.now(), Instant.now()
        );

        // Token hợp lệ (Hết hạn sau 24h)
        validTokenData = new VerificationTokenData(
            "valid-token-123", 
            "user-1", 
            Instant.now().plus(24, ChronoUnit.HOURS)
        );
    }

    // =========================================================================
    // 1. KỊCH BẢN THẤT BẠI: LỖI INPUT (Token rỗng)
    // =========================================================================
    @Test
    @DisplayName("Fail: Token đầu vào bị rỗng hoặc null")
    void test_Fail_EmptyToken() {
        VerifyEmailRequestData input = new VerifyEmailRequestData("");

        useCase.execute(input);

        ArgumentCaptor<VerifyEmailResponseData> captor = ArgumentCaptor.forClass(VerifyEmailResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        assertEquals("Mã xác thực không được để trống.", captor.getValue().message);
    }

    // =========================================================================
    // 2. KỊCH BẢN THẤT BẠI: TOKEN KHÔNG TỒN TẠI TRONG DB
    // =========================================================================
    @Test
    @DisplayName("Fail: Không tìm thấy Token trong DB")
    void test_Fail_TokenNotFound() {
        VerifyEmailRequestData input = new VerifyEmailRequestData("invalid-token");
        
        when(mockTokenRepo.findByToken("invalid-token")).thenReturn(null);

        useCase.execute(input);

        ArgumentCaptor<VerifyEmailResponseData> captor = ArgumentCaptor.forClass(VerifyEmailResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("không hợp lệ hoặc không tồn tại"));
    }

    // =========================================================================
    // 3. KỊCH BẢN THẤT BẠI: TOKEN ĐÃ HẾT HẠN
    // =========================================================================
    @Test
    @DisplayName("Fail: Token tìm thấy nhưng đã hết hạn")
    void test_Fail_TokenExpired() {
        VerifyEmailRequestData input = new VerifyEmailRequestData("expired-token");
        
        // Tạo token đã hết hạn cách đây 1 giờ
        VerificationTokenData expiredToken = new VerificationTokenData(
            "expired-token", "user-1", Instant.now().minus(1, ChronoUnit.HOURS)
        );
        
        when(mockTokenRepo.findByToken("expired-token")).thenReturn(expiredToken);

        useCase.execute(input);

        ArgumentCaptor<VerifyEmailResponseData> captor = ArgumentCaptor.forClass(VerifyEmailResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("đã hết hạn"));
    }

    // =========================================================================
    //	4. KỊCH BẢN THÀNH CÔNG (BIẾN THỂ): TÀI KHOẢN ĐÃ KÍCH HOẠT TRƯỚC ĐÓ
    // =========================================================================
    @Test
    @DisplayName("Success: Tài khoản đã Active rồi (Idempotency)")
    void test_Success_AlreadyActive() {
        VerifyEmailRequestData input = new VerifyEmailRequestData("valid-token-123");
        
        // User đã là ACTIVE
        pendingUser.status = AccountStatus.ACTIVE;
        
        when(mockTokenRepo.findByToken("valid-token-123")).thenReturn(validTokenData);
        when(mockUserRepo.findByUserId("user-1")).thenReturn(pendingUser);

        useCase.execute(input);

        // Verify: Vẫn xóa token
        verify(mockTokenRepo).deleteByToken("valid-token-123");
        // Verify: Không gọi update DB vì đã active rồi
        verify(mockUserRepo, never()).update(any());

        ArgumentCaptor<VerifyEmailResponseData> captor = ArgumentCaptor.forClass(VerifyEmailResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertTrue(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("đã được kích hoạt trước đó"));
    }

    // =========================================================================
    // 6. KỊCH BẢN THÀNH CÔNG (CHUẨN): KÍCH HOẠT LẦN ĐẦU
    // =========================================================================
    @Test
    @DisplayName("Success: Kích hoạt thành công -> Update User -> Xóa Token")
    void test_Success_Activation() {
        VerifyEmailRequestData input = new VerifyEmailRequestData("valid-token-123");
        
        when(mockTokenRepo.findByToken("valid-token-123")).thenReturn(validTokenData);
        when(mockUserRepo.findByUserId("user-1")).thenReturn(pendingUser);

        // WHEN
        useCase.execute(input);

        // THEN
        
        // 1. Verify User được cập nhật trạng thái ACTIVE
        ArgumentCaptor<UserData> userCaptor = ArgumentCaptor.forClass(UserData.class);
        verify(mockUserRepo).update(userCaptor.capture());
        
        assertEquals(AccountStatus.ACTIVE, userCaptor.getValue().status);
        assertNotNull(userCaptor.getValue().updatedAt); // Thời gian cập nhật phải mới
        
        // 2. Verify Token bị xóa (Cleanup)
        verify(mockTokenRepo).deleteByToken("valid-token-123");

        // 3. Verify Output
        ArgumentCaptor<VerifyEmailResponseData> resCaptor = ArgumentCaptor.forClass(VerifyEmailResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());

        assertTrue(resCaptor.getValue().success);
        assertTrue(resCaptor.getValue().message.contains("thành công"));
    }
}
