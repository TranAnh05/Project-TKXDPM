package User.AdminCreateUser;

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
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserOutputBoundary;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserRequestData;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserResponseData;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserUseCase;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

@ExtendWith(MockitoExtension.class)
public class AdminCreateUserUseCaseTest {
	@Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IUserRepository mockUserRepository;
    @Mock private IPasswordHasher mockPasswordHasher;
    @Mock private IUserIdGenerator mockUserIdGenerator;
    @Mock private AdminCreateUserOutputBoundary mockOutputBoundary;

    // 2. Lớp cần test
    private AdminCreateUserUseCase useCase;

    // 3. Dữ liệu mẫu
    private AdminCreateUserRequestData requestData;
    private AuthPrincipal adminPrincipal;
    
    @BeforeEach
    void setUp() {
        useCase = new AdminCreateUserUseCase(
            mockTokenValidator, mockUserRepository, mockPasswordHasher,
            mockUserIdGenerator, mockOutputBoundary
        );

        // Yêu cầu tạo một Admin mới
        requestData = new AdminCreateUserRequestData(
            "Bearer admin.token",
            "new.admin@example.com",
            "password123",
            "New",
            "Admin",
            "ADMIN",   // Chỉ định vai trò
            "ACTIVE"   // Chỉ định trạng thái
        );
        
        // Giả lập Admin thực hiện
        adminPrincipal = new AuthPrincipal("admin-123", "admin@e.com", UserRole.ADMIN);
    }
    
    /**
     * Test kịch bản THÀNH CÔNG
     */
    @Test
    void test_execute_success() {
        // --- ARRANGE ---
        // 1. Giả lập Token Admin hợp lệ
        when(mockTokenValidator.validate("Bearer admin.token")).thenReturn(adminPrincipal);
        // 2. Giả lập Email là duy nhất
        when(mockUserRepository.findByEmail("new.admin@example.com")).thenReturn(null);
        // 3. Giả lập các generator
        when(mockUserIdGenerator.generate()).thenReturn("new-admin-uuid-456");
        when(mockPasswordHasher.hash("password123")).thenReturn("hashed_new_password");
        
        // 4. "Bắt" (Capture) DTO được gửi đến CSDL
        ArgumentCaptor<UserData> userDataCaptor = ArgumentCaptor.forClass(UserData.class);
        when(mockUserRepository.save(userDataCaptor.capture())).thenAnswer(
            invocation -> invocation.getArgument(0)
        );
        
        ArgumentCaptor<AdminCreateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AdminCreateUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Kiểm tra các bước
        verify(mockTokenValidator).validate("Bearer admin.token");
        verify(mockUserRepository).findByEmail("new.admin@example.com");
        verify(mockUserIdGenerator).generate();
        verify(mockPasswordHasher).hash("password123");
        verify(mockUserRepository).save(any(UserData.class));
        
        // 2. Kiểm tra dữ liệu GỬI ĐẾN CSDL
        UserData capturedData = userDataCaptor.getValue();
        assertEquals("new-admin-uuid-456", capturedData.userId);
        assertEquals("new.admin@example.com", capturedData.email);
        assertEquals(UserRole.ADMIN, capturedData.role, "Role phải là ADMIN (do Admin chỉ định)");
        assertEquals(AccountStatus.ACTIVE, capturedData.status, "Status phải là ACTIVE (do Admin chỉ định)");
        
        // 3. Kiểm tra dữ liệu GỬI ĐẾN PRESENTER
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AdminCreateUserResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertEquals("Tạo tài khoản thành công!", presentedResponse.message);
        assertEquals("new-admin-uuid-456", presentedResponse.createdUserId);
        assertEquals(UserRole.ADMIN, presentedResponse.role);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Không phải Admin
     */
    @Test
    void test_execute_failure_notAdmin() {
        // --- ARRANGE ---
        // Giả lập người gọi là Customer
        AuthPrincipal customerPrincipal = new AuthPrincipal("cust-789", "cust@e.com", UserRole.CUSTOMER);
        when(mockTokenValidator.validate(anyString())).thenReturn(customerPrincipal);
            
        ArgumentCaptor<AdminCreateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AdminCreateUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AdminCreateUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Không có quyền truy cập.", presentedResponse.message);
        
        verify(mockUserRepository, never()).findByEmail(anyString());
    }
    
    /**
     * Test kịch bản THẤT BẠI: Email đã tồn tại
     */
    @Test
    void test_execute_failure_emailAlreadyExists() {
        // --- ARRANGE ---
        when(mockTokenValidator.validate("Bearer admin.token")).thenReturn(adminPrincipal);
        // 1. Giả lập Email ĐÃ TỒN TẠI
        when(mockUserRepository.findByEmail("new.admin@example.com")).thenReturn(new UserData());
            
        ArgumentCaptor<AdminCreateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AdminCreateUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AdminCreateUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Email này đã tồn tại.", presentedResponse.message);
        
        verify(mockUserRepository, never()).save(any(UserData.class));
    }
    
    /**
     * Test kịch bản THẤT BẠI: Dữ liệu không hợp lệ (Vai trò không tồn tại)
     */
    @Test
    void test_execute_failure_invalidRole() {
        // --- ARRANGE ---
        AdminCreateUserRequestData badRequest = new AdminCreateUserRequestData(
            "Bearer admin.token", "new.admin@example.com", "password123",
            "New", "Admin", "SUPER_USER", "ACTIVE" // <-- Vai trò không hợp lệ
        );
        
        when(mockTokenValidator.validate("Bearer admin.token")).thenReturn(adminPrincipal);
            
        ArgumentCaptor<AdminCreateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AdminCreateUserResponseData.class);

        // --- ACT ---
        useCase.execute(badRequest);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AdminCreateUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Vai trò (Role) không hợp lệ: SUPER_USER", presentedResponse.message);
        
        verify(mockUserRepository, never()).findByEmail(anyString());
    }
}
