package User.AdminUpdateUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Entities.AccountStatus;
import Entities.UserRole;
import usecase.ManageUser.IAuthTokenValidator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;
import usecase.ManageUser.AdminUpdateUser.AdminUpdateFullProfileUseCase;
import usecase.ManageUser.AdminUpdateUser.AdminUpdateUserOutputBoundary;
import usecase.ManageUser.AdminUpdateUser.AdminUpdateUserRequestData;
import usecase.ManageUser.AdminUpdateUser.AdminUpdateUserResponseData;
import usecase.ManageUser.ViewUserProfile.AuthPrincipal;

@ExtendWith(MockitoExtension.class)
public class AdminUpdateFullProfileUseCaseTest {
	// 1. Mock các dependencies
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IUserRepository mockUserRepository;
    @Mock private AdminUpdateUserOutputBoundary mockOutputBoundary;

    // 2. Lớp cần test
    private AdminUpdateFullProfileUseCase useCase;

    // 3. Dữ liệu mẫu
    private AdminUpdateUserRequestData requestData;
    private AuthPrincipal adminPrincipal;
    private UserData targetUserData;

    @BeforeEach
    void setUp() {
        useCase = new AdminUpdateFullProfileUseCase(
            mockTokenValidator,
            mockUserRepository,
            mockOutputBoundary
        );

        // Giả lập Admin
        adminPrincipal = new AuthPrincipal("admin-123", "admin@e.com", UserRole.ADMIN);
        
        // Dữ liệu MỚI Admin gửi lên
        requestData = new AdminUpdateUserRequestData(
            "Bearer admin.token",
            "user-456", // ID mục tiêu
            "new.email@example.com",
            "Jane",
            "Doe",
            "0909111222",
            "CUSTOMER",
            "SUSPENDED" // Admin đình chỉ tài khoản này
        );

        // Dữ liệu CŨ của User mục tiêu (target) trong CSDL
        targetUserData = new UserData(
            "user-456", "old.email@example.com", "h", "John", "Smith", 
            null, UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now(), Instant.now()
        );
    }
    
    /**
     * Test kịch bản THÀNH CÔNG
     */
    @Test
    void test_execute_success() {
        // --- ARRANGE ---
        // 1. Giả lập Token Admin hợp lệ
        when(mockTokenValidator.validate("Bearer admin.token")).thenReturn(adminPrincipal);
        // 2. Giả lập tìm thấy User mục tiêu
        when(mockUserRepository.findByUserId("user-456")).thenReturn(targetUserData);
        // 3. Giả lập Email mới là duy nhất
        when(mockUserRepository.findByEmail("new.email@example.com")).thenReturn(null);
        
        // 4. "Bắt" DTO được gửi đến CSDL
        ArgumentCaptor<UserData> userDataCaptor = ArgumentCaptor.forClass(UserData.class);
        when(mockUserRepository.update(userDataCaptor.capture())).thenAnswer(
            invocation -> userDataCaptor.getValue()
        );
        
        ArgumentCaptor<AdminUpdateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Kiểm tra các bước
        verify(mockTokenValidator).validate("Bearer admin.token");
        verify(mockUserRepository).findByUserId("user-456");
        verify(mockUserRepository).findByEmail("new.email@example.com"); // Đã check email mới
        verify(mockUserRepository).update(any(UserData.class));
        
        // 2. Kiểm tra dữ liệu GỬI ĐẾN CSDL
        UserData capturedData = userDataCaptor.getValue();
        assertEquals("Jane", capturedData.firstName);
        assertEquals("new.email@example.com", capturedData.email);
        assertEquals(AccountStatus.SUSPENDED, capturedData.status); // Đã bị đình chỉ
        assertEquals(UserRole.CUSTOMER, capturedData.role);
        assertNotNull(capturedData.updatedAt);
        
        // 3. Kiểm tra dữ liệu GỬI ĐẾN PRESENTER
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AdminUpdateUserResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertEquals("Cập nhật người dùng thành công.", presentedResponse.message);
        assertEquals("Jane", presentedResponse.firstName);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Không phải Admin
     */
    @Test
    void test_execute_failure_notAdmin() {
        // --- ARRANGE ---
        AuthPrincipal customerPrincipal = new AuthPrincipal("user-123", "cust@e.com", UserRole.CUSTOMER);
        when(mockTokenValidator.validate(anyString())).thenReturn(customerPrincipal);

        ArgumentCaptor<AdminUpdateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AdminUpdateUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Không có quyền truy cập.", presentedResponse.message);
        
        verify(mockUserRepository, never()).findByUserId(anyString());
        verify(mockUserRepository, never()).update(any(UserData.class));
    }
    
    /**
     * Test kịch bản THẤT BẠI: Admin tự sửa chính mình
     */
    @Test
    void test_execute_failure_adminUpdatesSelf() {
        // --- ARRANGE ---
        // 1. Admin hợp lệ
        when(mockTokenValidator.validate("Bearer admin.token")).thenReturn(adminPrincipal);
        // 2. Tạo request mà Admin tự sửa mình
        AdminUpdateUserRequestData selfRequest = new AdminUpdateUserRequestData(
            "Bearer admin.token", "admin-123", // ID mục tiêu = ID của Admin
            "admin@e.com", "Admin", "User", null, "ADMIN", "ACTIVE"
        );
        
        ArgumentCaptor<AdminUpdateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);

        // --- ACT ---
        useCase.execute(selfRequest);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AdminUpdateUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertTrue(presentedResponse.message.contains("Admin không thể tự cập nhật"));
        
        verify(mockUserRepository, never()).findByUserId(anyString());
    }
    
    /**
     * Test kịch bản THẤT BẠI: Email mới đã tồn tại
     */
    @Test
    void test_execute_failure_emailAlreadyExists() {
        // --- ARRANGE ---
        when(mockTokenValidator.validate("Bearer admin.token")).thenReturn(adminPrincipal);
        when(mockUserRepository.findByUserId("user-456")).thenReturn(targetUserData);
        // 1. Giả lập Email mới (new.email@example.com) ĐÃ TỒN TẠI
        when(mockUserRepository.findByEmail("new.email@example.com")).thenReturn(new UserData());
        
        ArgumentCaptor<AdminUpdateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AdminUpdateUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Email mới này đã tồn tại.", presentedResponse.message);
        
        verify(mockUserRepository, never()).update(any(UserData.class));
    }
    
    /**
     * Test kịch bản THẤT BẠI: Lỗi hệ thống (CSDL sập khi Update)
     */
    @Test
    void test_execute_failure_systemErrorOnUpdate() {
        // --- ARRANGE ---
        when(mockTokenValidator.validate("Bearer admin.token")).thenReturn(adminPrincipal);
        when(mockUserRepository.findByUserId("user-456")).thenReturn(targetUserData);
        when(mockUserRepository.findByEmail("new.email@example.com")).thenReturn(null);
        
        // 1. Giả lập CSDL sập khi UPDATE
        when(mockUserRepository.update(any(UserData.class)))
            .thenThrow(new RuntimeException("Database connection failed"));
            
        ArgumentCaptor<AdminUpdateUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        verify(mockUserRepository).update(any(UserData.class)); // Đã cố gọi update
        
        verify(mockOutputBoundary).present(responseCaptor.capture());
        AdminUpdateUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Đã xảy ra lỗi hệ thống không xác định.", presentedResponse.message);
    }
}
