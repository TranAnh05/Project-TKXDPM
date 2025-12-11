package User.AdminCreateUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IEmailService;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserOutputBoundary;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserRequestData;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserResponseData;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserUseCase;

@ExtendWith(MockitoExtension.class)
public class AdminCreateUserUseCaseTest {

    @Mock private IAuthTokenValidator tokenValidator;
    @Mock private IUserRepository userRepository;
    @Mock private IPasswordHasher passwordHasher;
    @Mock private IUserIdGenerator userIdGenerator;
    @Mock private IEmailService emailService;
    @Mock private AdminCreateUserOutputBoundary outputBoundary;

    @InjectMocks
    private AdminCreateUserUseCase useCase;

    // --- CAPTORS ---
    @Captor private ArgumentCaptor<AdminCreateUserResponseData> responseCaptor;
    @Captor private ArgumentCaptor<UserData> userDataCaptor;

    // --- TEST DATA ---
    private AdminCreateUserRequestData validRequest;
    private AuthPrincipal adminPrincipal;
    private AuthPrincipal customerPrincipal;

    private final String ADMIN_TOKEN = "admin_token_jwt";
    private final String NEW_USER_EMAIL = "newuser@example.com";
    private final String VALID_PASS = "Password123";
    private final String VALID_PHONE = "0901234567"; 

    @BeforeEach
    void setUp() {
        // Setup request chuẩn với Constructor đầy đủ (đã bao gồm phoneNumber)
        validRequest = new AdminCreateUserRequestData(
            ADMIN_TOKEN, 
            NEW_USER_EMAIL, 
            VALID_PASS, 
            "Nguyen", 
            "Van B", 
            VALID_PHONE, 
            "CUSTOMER", 
            "ACTIVE"
        );

        adminPrincipal = new AuthPrincipal("admin-id", "admin@sys.com", UserRole.ADMIN);
        customerPrincipal = new AuthPrincipal("user-id", "user@sys.com", UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("Case: Fail - không phải admin")
    void testExecute_NotAdmin_Fail() {
        when(tokenValidator.validate(any())).thenReturn(customerPrincipal);

        // WHEN
        useCase.execute(validRequest);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Không có quyền truy cập.", responseCaptor.getValue().message);
    }
    
    @Test
    @DisplayName("Case: Fail - Tên rỗng")
    void testExecute_EmptyFirstName_Fail() {
        // GIVEN
        when(tokenValidator.validate(any())).thenReturn(adminPrincipal);
        // FirstName để rỗng ""
        AdminCreateUserRequestData invalidReq = new AdminCreateUserRequestData(
            ADMIN_TOKEN, NEW_USER_EMAIL, VALID_PASS, "", "Van B", VALID_PHONE, "CUSTOMER", "ACTIVE"
        );

        // WHEN
        useCase.execute(invalidReq);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Tên không được để trống.", responseCaptor.getValue().message); 
    }

    @Test
    @DisplayName("Case: Fail - Mật khẩu quá ngắn")
    void testExecute_ShortPassword_Fail() {
        // GIVEN
        when(tokenValidator.validate(any())).thenReturn(adminPrincipal);
        AdminCreateUserRequestData invalidReq = new AdminCreateUserRequestData(
            ADMIN_TOKEN, NEW_USER_EMAIL, "123", "Nguyen", "Van B", VALID_PHONE, "CUSTOMER", "ACTIVE"
        );

        // WHEN
        useCase.execute(invalidReq);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertEquals("Mật khẩu phải có ít nhất 8 ký tự.", responseCaptor.getValue().message); 
    }

    @Test
    @DisplayName("Case: Fail - Role không hợp lệ")
    void testExecute_InvalidRole_Fail() {
        // GIVEN
        when(tokenValidator.validate(any())).thenReturn(adminPrincipal);
        AdminCreateUserRequestData invalidReq = new AdminCreateUserRequestData(
            ADMIN_TOKEN, NEW_USER_EMAIL, VALID_PASS, "Nguyen", "Van B", VALID_PHONE, "SUPER_ADMIN", "ACTIVE"
        );

        // WHEN
        useCase.execute(invalidReq);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertTrue(responseCaptor.getValue().message.contains("Vai trò (Role) không hợp lệ"));
    }

    @Test
    @DisplayName("Case: Fail - Status không tồn tại")
    void testExecute_InvalidStatus_Fail() {
        // GIVEN
        when(tokenValidator.validate(any())).thenReturn(adminPrincipal);
        // Status sai
        AdminCreateUserRequestData invalidReq = new AdminCreateUserRequestData(
            ADMIN_TOKEN, NEW_USER_EMAIL, VALID_PASS, "Nguyen", "Van B", VALID_PHONE, "CUSTOMER", "SLEEPING"
        );

        // WHEN
        useCase.execute(invalidReq);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        assertFalse(responseCaptor.getValue().success);
        assertTrue(responseCaptor.getValue().message.contains("Trạng thái (Status) không hợp lệ"));
    }

    @Test
    @DisplayName("Case: Fail -Email không đúng định dạng")
    void testExecute_InvalidEmail_Fail() {
        // GIVEN
        when(tokenValidator.validate(any())).thenReturn(adminPrincipal);
        
        AdminCreateUserRequestData invalidReq = new AdminCreateUserRequestData(
            ADMIN_TOKEN, "invalid-email", VALID_PASS, "A", "B", VALID_PHONE, "CUSTOMER", "ACTIVE"
        );

        // WHEN
        useCase.execute(invalidReq);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        assertTrue(responseCaptor.getValue().message.contains("Email không đúng định dạng"));
    }
    
    @Test
    @DisplayName("Case: Fail - SĐT không đúng định dạng")
    void testExecute_InvalidPhone_Fail() {
        // GIVEN
        when(tokenValidator.validate(any())).thenReturn(adminPrincipal);
        
        AdminCreateUserRequestData invalidReq = new AdminCreateUserRequestData(
            ADMIN_TOKEN, NEW_USER_EMAIL, VALID_PASS, "A", "B", "123abc456", "CUSTOMER", "ACTIVE"
        );

        // WHEN
        useCase.execute(invalidReq);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        assertTrue(responseCaptor.getValue().message.contains("Số điện thoại không đúng định dạng"));
    }

    @Test
    @DisplayName("Case: Fail - Trùng email")
    void testExecute_DuplicateEmail_Fail() {
        // GIVEN
        when(tokenValidator.validate(any())).thenReturn(adminPrincipal);
        when(userRepository.findByEmail(NEW_USER_EMAIL)).thenReturn(new UserData(null, null, null, null, null, null, null, null, null, null));

        // WHEN
        useCase.execute(validRequest);

        // THEN
        verify(outputBoundary).present(responseCaptor.capture());
        assertEquals("Email này đã tồn tại.", responseCaptor.getValue().message);
        verify(emailService, never()).sendAccountCreatedEmail(any(), any(), any());
    }


    @Test
    @DisplayName("Case: Success - Create User & Send Email")
    void testExecute_Success() {
        // 1. ARRANGE
        when(tokenValidator.validate(any())).thenReturn(adminPrincipal);
        when(userRepository.findByEmail(any())).thenReturn(null);
        when(userIdGenerator.generate()).thenReturn("user-001");
        when(passwordHasher.hash(VALID_PASS)).thenReturn("hashed_pass_123");
        
        UserData savedData = new UserData(
            "user-001", NEW_USER_EMAIL, "hashed_pass_123", "Nguyen", "Van B", VALID_PHONE, 
            UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(userRepository.save(any(UserData.class))).thenReturn(savedData);

        // 2. ACT
        useCase.execute(validRequest);

        // 3. ASSERT
        verify(outputBoundary).present(responseCaptor.capture());
        AdminCreateUserResponseData res = responseCaptor.getValue();

        // Check Output
        assertTrue(res.success);
        assertEquals("Tạo tài khoản thành công!", res.message);
        assertEquals("user-001", res.createdUserId);
        assertEquals("Nguyen Van B", res.fullName);

        // Check Data gửi xuống Repository
        verify(userRepository).save(userDataCaptor.capture());
        UserData dbData = userDataCaptor.getValue();
        assertEquals("hashed_pass_123", dbData.hashedPassword); 
        assertEquals(VALID_PHONE, dbData.phoneNumber); 

        verify(emailService, times(1)).sendAccountCreatedEmail(
            eq(NEW_USER_EMAIL), 
            eq("Nguyen"), 
            eq(VALID_PASS) 
        );
    }
}