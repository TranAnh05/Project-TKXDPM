package User.AuthenticateUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageUser.IAuthTokenGenerator;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserOutputBoundary;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserRequestData;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserResponseData;
import cgx.com.usecase.ManageUser.AuthenticateUser.LoginByEmailUseCase;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserResponseData;

@ExtendWith(MockitoExtension.class)
public class LoginByEmailUseCaseTest {
	// 1. Mock các dependencies
    @Mock private IUserRepository mockUserRepository;
    @Mock private IPasswordHasher mockPasswordHasher;
    @Mock private IAuthTokenGenerator mockTokenGenerator;
    @Mock private AuthenticateUserOutputBoundary mockOutputBoundary;

    // 2. Lớp cần test
    private LoginByEmailUseCase useCase;

    // 3. Dữ liệu mẫu
    private AuthenticateUserRequestData requestData;
    private UserData validUserData;
    
    @BeforeEach
    void setUp() {
        useCase = new LoginByEmailUseCase(
            mockUserRepository,
            mockPasswordHasher,
            mockTokenGenerator,
            mockOutputBoundary
        );

        requestData = new AuthenticateUserRequestData("test@example.com", "password123");

        validUserData = new UserData();
        validUserData.userId = "user-123";
        validUserData.email = "test@example.com";
        validUserData.hashedPassword = "hashed_password_abc";
        validUserData.role = UserRole.CUSTOMER;
        validUserData.status = AccountStatus.ACTIVE;
    }

    /**
     * Test kịch bản THÀNH CÔNG
     */
    @Test
    void test_execute_success() {
        // --- ARRANGE ---
        // 1. Giả lập tìm thấy User
        when(mockUserRepository.findByEmail("test@example.com")).thenReturn(validUserData);
        // 2. Giả lập mật khẩu ĐÚNG
        when(mockPasswordHasher.verify("password123", "hashed_password_abc")).thenReturn(true);
        // 3. Giả lập tạo token
        when(mockTokenGenerator.generate("user-123", "test@example.com", UserRole.CUSTOMER))
            .thenReturn("jwt.token.string");
        
        ArgumentCaptor<AuthenticateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AuthenticateUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        verify(mockUserRepository).findByEmail("test@example.com");
        verify(mockPasswordHasher).verify("password123", "hashed_password_abc");
        verify(mockTokenGenerator).generate("user-123", "test@example.com", UserRole.CUSTOMER);
        
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AuthenticateUserResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertEquals("Đăng nhập thành công!", presentedResponse.message);
        assertEquals("jwt.token.string", presentedResponse.token);
    }
    

    /**
     * Test kịch bản THẤT BẠI: Không tìm thấy User
     */
    @Test
    void test_execute_failure_userNotFound() {
        // --- ARRANGE ---
        when(mockUserRepository.findByEmail("test@example.com")).thenReturn(null);
        ArgumentCaptor<AuthenticateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AuthenticateUserResponseData.class);
            
        // --- ACT ---
        useCase.execute(requestData);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AuthenticateUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Sai thông tin đăng nhập.", presentedResponse.message);
        
        // Quan trọng: Không bao giờ gọi hàm so sánh mật khẩu
        verify(mockPasswordHasher, never()).verify(toString(), toString());
        verify(mockTokenGenerator, never()).generate(any(), any(), any());
    }
    
    /**
     * Test kịch bản THẤT BẠI: Sai mật khẩu
     */
    @Test
    void test_execute_failure_wrongPassword() {
        // --- ARRANGE ---
        when(mockUserRepository.findByEmail("test@example.com")).thenReturn(validUserData);
        // 2. Giả lập mật khẩu SAI
        when(mockPasswordHasher.verify("password123", "hashed_password_abc")).thenReturn(false);
        
        ArgumentCaptor<AuthenticateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AuthenticateUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AuthenticateUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Sai thông tin đăng nhập.", presentedResponse.message);
        
        // Quan trọng: Không bao H giờ gọi hàm tạo token
        verify(mockTokenGenerator, never()).generate(any(), any(), any());
    }
    
    /**
     * Test kịch bản THẤT BẠI: Tài khoản bị khóa (SUSPENDED)
     */
    @Test
    void test_execute_failure_accountSuspended() {
        // --- ARRANGE ---
        validUserData.status = AccountStatus.SUSPENDED; // <-- Thay đổi trạng thái
        when(mockUserRepository.findByEmail("test@example.com")).thenReturn(validUserData);
        when(mockPasswordHasher.verify("password123", "hashed_password_abc")).thenReturn(true); // Vẫn đúng pass

        ArgumentCaptor<AuthenticateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AuthenticateUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AuthenticateUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Tài khoản đã bị khóa hoặc chưa kích hoạt.", presentedResponse.message);
        
        // Quan trọng: Vẫn check pass, nhưng KHÔNG tạo token
        verify(mockPasswordHasher).verify("password123", "hashed_password_abc");
        verify(mockTokenGenerator, never()).generate(any(), any(), any());
    }
    
    /**
     * Test kịch bản THẤT BẠI: Mật khẩu để trống (Lỗi Validation)
     */
    @Test
    void test_execute_failure_passwordEmpty() {
        // --- ARRANGE ---
        AuthenticateUserRequestData badRequest = 
            new AuthenticateUserRequestData("test@example.com", ""); // Pass rỗng
            
        ArgumentCaptor<AuthenticateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AuthenticateUserResponseData.class);

        // --- ACT ---
        useCase.execute(badRequest);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AuthenticateUserResponseData presentedResponse = responseCaptor.getValue();
        
        assertFalse(presentedResponse.success);
        assertEquals("Mật khẩu không được để trống.", presentedResponse.message);

        // Dừng ngay từ đầu, không gọi CSDL
        verify(mockUserRepository, never()).findByEmail(anyString());
    }
    
    /**
     * Test kịch bản THẤT BẠI: CSDL sập
     */
    @Test
    void test_execute_failure_DBFallOff() {
        // --- ARRANGE ---
        AuthenticateUserRequestData badRequest = 
            new AuthenticateUserRequestData("test@example.com", "password123");
            
        when(mockUserRepository.findByEmail("test@example.com")).thenThrow(new RuntimeException("Database connection failed"));

        ArgumentCaptor<AuthenticateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AuthenticateUserResponseData.class);
        

        // --- ACT ---
        useCase.execute(badRequest);
        
        // --- ASSERT ---
     // --- ASSERT ---
        // 2. Kiểm tra response lỗi hệ thống
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AuthenticateUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Đã xảy ra lỗi hệ thống không xác định.", presentedResponse.message);
    }
}
