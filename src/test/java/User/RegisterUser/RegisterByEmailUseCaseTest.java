package User.RegisterUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
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
import cgx.com.usecase.ManageUser.IEmailService;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.ISecureTokenGenerator;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.IVerificationTokenRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.VerificationTokenData;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterByEmailUseCase;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserOutputBoundary;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserRequestData;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserResponseData;

/**
 * Unit Test cho Use Case (Interactor) - Layer 3
 * Chúng ta test RegisterByEmailUseCase (lớp Con)
 * nhưng thực chất là đang test logic của AbstractRegisterUserUseCase (lớp Cha).
 */
@ExtendWith(MockitoExtension.class)
public class RegisterByEmailUseCaseTest {

    // 1. Mock Dependencies
    @Mock private IUserRepository mockUserRepo;
    @Mock private IPasswordHasher mockPasswordHasher;
    @Mock private IUserIdGenerator mockUserIdGenerator;
    @Mock private IEmailService mockEmailService;
    @Mock private ISecureTokenGenerator mockTokenGenerator;
    @Mock private IVerificationTokenRepository mockTokenRepository;
    @Mock private RegisterUserOutputBoundary mockOutputBoundary;

    // 2. Class under test
    private RegisterByEmailUseCase useCase;

    // 3. Test Data
    private RegisterUserRequestData validRequest;
    private UserData savedUserData;

    @BeforeEach
    void setUp() {
        // Khởi tạo UseCase với đầy đủ dependencies (bao gồm cả Token Repo mới)
        useCase = new RegisterByEmailUseCase(
            mockUserRepo, 
            mockPasswordHasher, 
            mockUserIdGenerator, 
            mockEmailService, 
            mockTokenGenerator, 
            mockTokenRepository, 
            mockOutputBoundary
        );

        // Input hợp lệ
        validRequest = new RegisterUserRequestData("test@mail.com", "password123", "Nguyen", "Van A");

        // Dữ liệu User giả lập sau khi save
        savedUserData = new UserData(
            "user-id-1", "test@mail.com", "hashed_pass", "Nguyen", "Van A", 
            null, UserRole.CUSTOMER, AccountStatus.PENDING_VERIFICATION, null, null
        );
    }

    // =========================================================================
    // 1. KỊCH BẢN THẤT BẠI: LỖI VALIDATION (Input không hợp lệ)
    // =========================================================================
    @Test
    @DisplayName("Fail: Email không đúng định dạng")
    void test_Fail_InvalidEmail() {
        RegisterUserRequestData input = new RegisterUserRequestData("invalid-email", "pass123", "A", "B");

        useCase.execute(input);

        ArgumentCaptor<RegisterUserResponseData> captor = ArgumentCaptor.forClass(RegisterUserResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        // Lỗi này từ User.validateEmail
        assertTrue(captor.getValue().message.contains("Email không đúng định dạng"));
    }
    
    @Test
    @DisplayName("Fail: Mật khẩu quá ngắn (Logic riêng của RegisterByEmail)")
    void test_Fail_WeakPassword() {
        // Password chỉ có 3 ký tự (yêu cầu min 8)
        RegisterUserRequestData input = new RegisterUserRequestData("valid@mail.com", "123", "A", "B");

        useCase.execute(input);

        ArgumentCaptor<RegisterUserResponseData> captor = ArgumentCaptor.forClass(RegisterUserResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        // Lỗi này từ User.validatePassword
        assertTrue(captor.getValue().message.contains("ít nhất 8 ký tự"));
    }

    // =========================================================================
    // 2. KỊCH BẢN THẤT BẠI: EMAIL ĐÃ TỒN TẠI (Business Rule)
    // =========================================================================
    @Test
    @DisplayName("Fail: Email đã được đăng ký trước đó")
    void test_Fail_DuplicateEmail() {
        // GIVEN: Repo tìm thấy user với email này
        when(mockUserRepo.findByEmail(validRequest.email)).thenReturn(savedUserData);

        // WHEN
        useCase.execute(validRequest);

        // THEN
        ArgumentCaptor<RegisterUserResponseData> captor = ArgumentCaptor.forClass(RegisterUserResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        assertEquals("Email này đã tồn tại.", captor.getValue().message);
        
        // Verify không gọi save
        verify(mockUserRepo, never()).save(any());
    }

    // =========================================================================
    // 3. KỊCH BẢN THẤT BẠI: LỖI HỆ THỐNG (Database Error)
    // =========================================================================
    @Test
    @DisplayName("Fail: Lỗi DB khi lưu User")
    void test_Fail_SystemError() {
        // GIVEN
        when(mockUserRepo.findByEmail(validRequest.email)).thenReturn(null);
        when(mockUserIdGenerator.generate()).thenReturn("user-id-1");
        when(mockPasswordHasher.hash(validRequest.password)).thenReturn("hashed_pass");
        
        // Giả lập lỗi khi save user
        doThrow(new RuntimeException("DB Timeout")).when(mockUserRepo).save(any());

        // WHEN
        useCase.execute(validRequest);

        // THEN
        ArgumentCaptor<RegisterUserResponseData> captor = ArgumentCaptor.forClass(RegisterUserResponseData.class);
        verify(mockOutputBoundary).present(captor.capture());

        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("lỗi hệ thống"));
    }

    // =========================================================================
    // 4. KỊCH BẢN THÀNH CÔNG: HAPPY PATH (Đúng logic thực tế)
    // =========================================================================
    @Test
    @DisplayName("Success: Đăng ký thành công -> Lưu Token -> Gửi Mail -> Trả về PENDING")
    void test_Success() {
        // GIVEN
        when(mockUserRepo.findByEmail(validRequest.email)).thenReturn(null);
        
        // 1. Mock tạo ID và Hash Pass
        when(mockUserIdGenerator.generate()).thenReturn("user-id-1");
        when(mockPasswordHasher.hash("password123")).thenReturn("hashed_pass");
        
        // 2. Mock lưu User thành công
        when(mockUserRepo.save(any(UserData.class))).thenReturn(savedUserData);
        
        // 3. Mock tạo Token xác thực
        when(mockTokenGenerator.generate()).thenReturn("secure-token-123");

        // WHEN
        useCase.execute(validRequest);

        // THEN
        // A. Verify User được lưu đúng
        ArgumentCaptor<UserData> userCaptor = ArgumentCaptor.forClass(UserData.class);
        verify(mockUserRepo).save(userCaptor.capture());
        UserData capturedUser = userCaptor.getValue();
        assertEquals("test@mail.com", capturedUser.email);
        assertEquals("hashed_pass", capturedUser.hashedPassword);
        assertEquals(AccountStatus.PENDING_VERIFICATION, capturedUser.status); // Quan trọng: Phải là PENDING
        
        // B. Verify Token được lưu vào DB
        ArgumentCaptor<VerificationTokenData> tokenCaptor = ArgumentCaptor.forClass(VerificationTokenData.class);
        verify(mockTokenRepository).save(tokenCaptor.capture());
        VerificationTokenData capturedToken = tokenCaptor.getValue();
        assertEquals("secure-token-123", capturedToken.token);
        assertEquals("user-id-1", capturedToken.userId);
        assertNotNull(capturedToken.expiryDate); // Phải có hạn dùng

        // C. Verify Email được gửi đi
        verify(mockEmailService).sendVerificationEmail(
            eq("test@mail.com"), 
            contains("Nguyen Van A"), // Check tên người nhận
            eq("secure-token-123")    // Check đúng token được gửi
        );

        // D. Verify Output trả về cho UI
        ArgumentCaptor<RegisterUserResponseData> resCaptor = ArgumentCaptor.forClass(RegisterUserResponseData.class);
        verify(mockOutputBoundary).present(resCaptor.capture());
        
        assertTrue(resCaptor.getValue().success);
        // Message phải nhắc user check mail
        assertTrue(resCaptor.getValue().message.contains("kiểm tra email")); 
        assertEquals("user-id-1", resCaptor.getValue().createdUserId);
    }
}
