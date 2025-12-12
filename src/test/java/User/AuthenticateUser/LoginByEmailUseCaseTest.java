package User.AuthenticateUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import cgx.com.usecase.Interface_Common.IAuthTokenGenerator;
import cgx.com.usecase.Interface_Common.IPasswordHasher;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserOutputBoundary;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserRequestData;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserResponseData;
import cgx.com.usecase.ManageUser.AuthenticateUser.LoginByEmailUseCase;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserResponseData;

@ExtendWith(MockitoExtension.class)
public class LoginByEmailUseCaseTest {

    // 1. Mock Dependencies
    @Mock private IUserRepository mockUserRepo;
    @Mock private IPasswordHasher mockPasswordHasher;
    @Mock private IAuthTokenGenerator mockTokenGenerator;
    @Mock private AuthenticateUserOutputBoundary mockOutputBoundary;

    // 2. Class under test
    private LoginByEmailUseCase useCase;

    // 3. Test Data
    private AuthenticateUserRequestData validRequest;
    private UserData activeUser;

    @BeforeEach
    void setUp() {
        useCase = new LoginByEmailUseCase(mockUserRepo, mockPasswordHasher, mockTokenGenerator, mockOutputBoundary);

        // Input hợp lệ
        validRequest = new AuthenticateUserRequestData("test@mail.com", "password123");

        // User trong DB (Trạng thái ACTIVE)
        activeUser = new UserData(
            "user-1", 
            "test@mail.com", 
            "hashed_pass", 
            "Nguyen", 
            "Van A", 
            "0901234567", 
            UserRole.CUSTOMER, 
            AccountStatus.ACTIVE, 
            Instant.now(), 
            Instant.now()
        );
    }

    @Test
    @DisplayName("Fail: Email không đúng định dạng")
    void test_Fail_InvalidEmailFormat() {
        AuthenticateUserRequestData input = new AuthenticateUserRequestData("invalid-email", "pass123");

        useCase.execute(input);

        ArgumentCaptor<AuthenticateUserResponseData> captor = ArgumentCaptor.forClass(AuthenticateUserResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        // Lỗi từ User.validateEmail
        assertTrue(captor.getValue().message.contains("Email không đúng định dạng"));
    }

    @Test
    @DisplayName("Fail: Mật khẩu để trống (Validate Type Specific)")
    void test_Fail_InvalidPasswordFormat() {
        AuthenticateUserRequestData input = new AuthenticateUserRequestData("test@mail.com", "");

        useCase.execute(input);

        ArgumentCaptor<AuthenticateUserResponseData> captor = ArgumentCaptor.forClass(AuthenticateUserResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        // Lỗi từ User.validatePassword
        assertTrue(captor.getValue().message.contains("Mật khẩu không được để trống"));
    }

    @Test
    @DisplayName("Fail: Không tìm thấy tài khoản trong DB")
    void test_Fail_UserNotFound() {
        // Mock Repo trả về null
        when(mockUserRepo.findByEmail("test@mail.com")).thenReturn(null);

        useCase.execute(validRequest);

        ArgumentCaptor<AuthenticateUserResponseData> captor = ArgumentCaptor.forClass(AuthenticateUserResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy tài khoản.", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Sai mật khẩu")
    void test_Fail_WrongPassword() {
        // Mock Repo tìm thấy user
        when(mockUserRepo.findByEmail("test@mail.com")).thenReturn(activeUser);
        
        // Mock Hasher trả về false (Không khớp)
        when(mockPasswordHasher.verify("password123", "hashed_pass")).thenReturn(false);

        useCase.execute(validRequest);

        ArgumentCaptor<AuthenticateUserResponseData> captor = ArgumentCaptor.forClass(AuthenticateUserResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        // Lỗi từ LoginByEmailUseCase.authenticate -> SecurityException
        assertEquals("Sai thông tin đăng nhập.", captor.getValue().message);
    }

    @Test
    @DisplayName("Fail: Tài khoản bị khóa (SUSPENDED)")
    void test_Fail_AccountSuspended() {
        activeUser.status = AccountStatus.SUSPENDED;

        when(mockUserRepo.findByEmail("test@mail.com")).thenReturn(activeUser);
        when(mockPasswordHasher.verify("password123", "hashed_pass")).thenReturn(true);

        useCase.execute(validRequest);

        ArgumentCaptor<AuthenticateUserResponseData> captor = ArgumentCaptor.forClass(AuthenticateUserResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        // Lỗi từ User.isNotLogin
        assertTrue(captor.getValue().message.contains("không được phép đăng nhập"));
    }

    @Test
    @DisplayName("Success: Đăng nhập thành công -> Trả về Token")
    void test_Success() {
        // GIVEN
        when(mockUserRepo.findByEmail("test@mail.com")).thenReturn(activeUser);
        when(mockPasswordHasher.verify("password123", "hashed_pass")).thenReturn(true);
        when(mockTokenGenerator.generate("user-1", "test@mail.com", UserRole.CUSTOMER))
            .thenReturn("valid.jwt.token");

        // WHEN
        useCase.execute(validRequest);

        // THEN
        ArgumentCaptor<AuthenticateUserResponseData> captor = ArgumentCaptor.forClass(AuthenticateUserResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        AuthenticateUserResponseData response = captor.getValue();
        assertTrue(response.success);
        assertEquals("Đăng nhập thành công!", response.message);
        assertEquals("valid.jwt.token", response.token);
        assertEquals("user-1", response.userId);
        assertEquals(UserRole.CUSTOMER, response.role);
    }
}