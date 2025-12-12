package User.ChangePassword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.Interface_Common.IEmailService;
import cgx.com.usecase.Interface_Common.IPasswordHasher;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.ChangePassword.ChangePasswordOutputBoundary;
import cgx.com.usecase.ManageUser.ChangePassword.ChangePasswordRequestData;
import cgx.com.usecase.ManageUser.ChangePassword.ChangePasswordResponseData;
import cgx.com.usecase.ManageUser.ChangePassword.ChangePasswordUseCase;

@ExtendWith(MockitoExtension.class)
public class ChangePasswordUseCaseTest {

    @Mock
    private IAuthTokenValidator tokenValidator;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IPasswordHasher passwordHasher;

    @Mock
    private IEmailService emailService; // Mock thêm service Email

    @Mock
    private ChangePasswordOutputBoundary outputBoundary;

    @InjectMocks
    private ChangePasswordUseCase changePasswordUseCase;

    @Captor
    private ArgumentCaptor<ChangePasswordResponseData> responseCaptor;

    @Captor
    private ArgumentCaptor<UserData> userDataCaptor;

    private ChangePasswordRequestData request;
    private UserData mockUserData;
    private AuthPrincipal mockPrincipal;

    private final String VALID_TOKEN = "valid_token_abc";
    private final String USER_ID = "user_123";
    private final String EMAIL = "test@example.com";
    private final String FIRST_NAME = "Nguyen";
    private final String OLD_HASHED_PASS = "hashed_old_pass";
    private final String OLD_PLAIN_PASS = "oldPassword123"; // > 8 chars
    private final String NEW_PLAIN_PASS = "newPassword456"; // > 8 chars

    @BeforeEach
    void setUp() {
        // Setup dữ liệu input mặc định
        request = new ChangePasswordRequestData(VALID_TOKEN, OLD_PLAIN_PASS, NEW_PLAIN_PASS);

        // Setup UserData giả lập trả về từ DB
        mockUserData = new UserData(
            USER_ID, EMAIL, OLD_HASHED_PASS, 
            FIRST_NAME, "Van A", "0909000111", 
            UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now(), Instant.now()
        );

        // Setup Principal giả lập trả về từ Token Validator
        mockPrincipal = new AuthPrincipal(USER_ID, EMAIL, UserRole.CUSTOMER);
    }

    // --- CASE 1: Token không hợp lệ 
    @Test
    void testExecute_InvalidToken_Fail() {
        // GIVEN
        doThrow(new SecurityException("Invalid Token")).when(tokenValidator).validate(request.authToken);

        // WHEN
        changePasswordUseCase.execute(request);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        ChangePasswordResponseData response = responseCaptor.getValue();
        
        assert !response.success;
        assert response.message.equals("Invalid Token");
        
        verify(userRepository, never()).findByUserId(any());
    }

    // --- CASE 2: Mật khẩu cũ quá ngắn/rỗng
    @Test
    void testExecute_InvalidOldPasswordFormat_Fail() {
        // GIVEN
        when(tokenValidator.validate(request.authToken)).thenReturn(mockPrincipal);
        request.oldPassword = "123"; // Quá ngắn (< 8 chars)

        // WHEN
        changePasswordUseCase.execute(request);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        ChangePasswordResponseData response = responseCaptor.getValue();
        
        assert !response.success;
        assert response.message.contains("ít nhất 8 ký tự"); 
    }

    // --- CASE 3: Mật khẩu mới quá ngắn/rỗng
    @Test
    void testExecute_InvalidNewPasswordFormat_Fail() {
        // GIVEN
        when(tokenValidator.validate(request.authToken)).thenReturn(mockPrincipal);
        request.newPassword = ""; 

        // WHEN
        changePasswordUseCase.execute(request);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        ChangePasswordResponseData response = responseCaptor.getValue();

        assert !response.success;
        assert response.message.contains("không được để trống");
    }

    // --- CASE 4: Sai mật khẩu cũ 
    @Test
    void testExecute_WrongOldPassword_Fail() {
        // GIVEN
        when(tokenValidator.validate(request.authToken)).thenReturn(mockPrincipal);
        when(userRepository.findByUserId(USER_ID)).thenReturn(mockUserData);
        
        // Giả lập verify trả về false
        when(passwordHasher.verify(request.oldPassword, OLD_HASHED_PASS)).thenReturn(false);

        // WHEN
        changePasswordUseCase.execute(request);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        ChangePasswordResponseData response = responseCaptor.getValue();

        assert !response.success;
        assert response.message.equals("Mật khẩu cũ không chính xác.");
        
        verify(userRepository, never()).update(any());
        verify(emailService, never()).sendPasswordChangeAlert(any(), any());
    }

    // --- CASE 5: Mật khẩu mới trùng mật khẩu cũ ---
    @Test
    void testExecute_NewPasswordSameAsOld_Fail() {
        // GIVEN
        when(tokenValidator.validate(request.authToken)).thenReturn(mockPrincipal);
        when(userRepository.findByUserId(USER_ID)).thenReturn(mockUserData);
        when(passwordHasher.verify(request.oldPassword, OLD_HASHED_PASS)).thenReturn(true);
        
        // Set pass mới giống hệt pass cũ
        request.newPassword = request.oldPassword;

        // WHEN
        changePasswordUseCase.execute(request);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        ChangePasswordResponseData response = responseCaptor.getValue();

        assert !response.success;
        assert response.message.equals("Mật khẩu mới không được trùng với mật khẩu cũ.");
        
        verify(userRepository, never()).update(any());
        verify(emailService, never()).sendPasswordChangeAlert(any(), any());
    }
    
    // --- CASE 7: THÀNH CÔNG 
    @Test
    void testExecute_Success() {
        // GIVEN
        when(tokenValidator.validate(request.authToken)).thenReturn(mockPrincipal);
        when(userRepository.findByUserId(USER_ID)).thenReturn(mockUserData);
        
        // 1. Verify pass cũ đúng
        when(passwordHasher.verify(request.oldPassword, OLD_HASHED_PASS)).thenReturn(true);
        // 2. Hash pass mới
        String newHashedVal = "hashed_new_pass_789";
        when(passwordHasher.hash(request.newPassword)).thenReturn(newHashedVal);

        // WHEN
        changePasswordUseCase.execute(request);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        ChangePasswordResponseData response = responseCaptor.getValue();
        
        // 1. Kiểm tra kết quả trả về
        assert response.success;
        assert response.message.equals("Đổi mật khẩu thành công.");

        // 2. Kiểm tra DB được update
        verify(userRepository, times(1)).update(userDataCaptor.capture());
        UserData updatedData = userDataCaptor.getValue();
        assert updatedData.hashedPassword.equals(newHashedVal); // Pass mới đã lưu
        
        // 3. Kiểm tra Email cảnh báo đã được gửi
        // Phải đảm bảo gửi đúng email và đúng tên của user
        verify(emailService, times(1)).sendPasswordChangeAlert(eq(EMAIL), eq(FIRST_NAME));
    }
}
