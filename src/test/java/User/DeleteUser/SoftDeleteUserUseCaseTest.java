package User.DeleteUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import usecase.ManageUser.DeleteUser.DeleteUserOutputBoundary;
import usecase.ManageUser.DeleteUser.DeleteUserRequestData;
import usecase.ManageUser.DeleteUser.DeleteUserResponseData;
import usecase.ManageUser.DeleteUser.SoftDeleteUserUseCase;
import usecase.ManageUser.ViewUserProfile.AuthPrincipal;

@ExtendWith(MockitoExtension.class)
public class SoftDeleteUserUseCaseTest {
	// 1. Mock các dependencies
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IUserRepository mockUserRepository;
    @Mock private DeleteUserOutputBoundary mockOutputBoundary;

    // 2. Lớp cần test
    private SoftDeleteUserUseCase useCase;

    // 3. Dữ liệu mẫu
    private DeleteUserRequestData requestData;
    private AuthPrincipal adminPrincipal;
    private UserData targetUserData;

    @BeforeEach
    void setUp() {
        useCase = new SoftDeleteUserUseCase(
            mockTokenValidator,
            mockUserRepository,
            mockOutputBoundary
        );

        // Giả lập Admin
        adminPrincipal = new AuthPrincipal("admin-123", "admin@e.com", UserRole.ADMIN);
        
        // Yêu cầu xóa "user-456"
        requestData = new DeleteUserRequestData(
            "Bearer admin.token",
            "user-456" // ID mục tiêu
        );

        // Dữ liệu CŨ của User mục tiêu (target) trong CSDL
        targetUserData = new UserData(
            "user-456", "customer@example.com", "h", "John", "Doe", 
            "0909123456", UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now(), Instant.now()
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
        
        // 3. "Bắt" DTO được gửi đến CSDL
        ArgumentCaptor<UserData> userDataCaptor = ArgumentCaptor.forClass(UserData.class);
        when(mockUserRepository.update(userDataCaptor.capture())).thenAnswer(
            invocation -> userDataCaptor.getValue()
        );
        
        ArgumentCaptor<DeleteUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(DeleteUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Kiểm tra các bước
        verify(mockTokenValidator).validate("Bearer admin.token");
        verify(mockUserRepository).findByUserId("user-456");
        verify(mockUserRepository).update(any(UserData.class));
        
        // 2. Kiểm tra dữ liệu GỬI ĐẾN CSDL (Đã được Xóa mềm)
        UserData capturedData = userDataCaptor.getValue();
        assertEquals("Deleted", capturedData.firstName);
        assertEquals("User", capturedData.lastName);
        assertNull(capturedData.phoneNumber);
        assertEquals(AccountStatus.DELETED, capturedData.status);
        assertTrue(capturedData.email.startsWith("deleted_user-456_")); // Đã ẩn danh
        assertTrue(capturedData.hashedPassword.startsWith("DELETED_")); // Đã vô hiệu hóa
        assertNotNull(capturedData.updatedAt);
        
        // 3. Kiểm tra dữ liệu GỬI ĐẾN PRESENTER
        verify(mockOutputBoundary).present(responseCaptor.capture());
        DeleteUserResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertEquals("Xóa người dùng thành công.", presentedResponse.message);
        assertEquals("user-456", presentedResponse.deletedUserId);
        assertEquals("DELETED", presentedResponse.newStatus);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Không phải Admin
     */
    @Test
    void test_execute_failure_notAdmin() {
        // --- ARRANGE ---
        AuthPrincipal customerPrincipal = new AuthPrincipal("user-123", "cust@e.com", UserRole.CUSTOMER);
        when(mockTokenValidator.validate(anyString())).thenReturn(customerPrincipal);

        ArgumentCaptor<DeleteUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(DeleteUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        DeleteUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Không có quyền truy cập.", presentedResponse.message);
        
        verify(mockUserRepository, never()).update(any(UserData.class));
    }

    /**
     * Test kịch bản THẤT BẠI: Admin tự xóa chính mình
     */
    @Test
    void test_execute_failure_adminDeletesSelf() {
        // --- ARRANGE ---
        when(mockTokenValidator.validate("Bearer admin.token")).thenReturn(adminPrincipal);
        
        // Tạo request Admin tự xóa mình
        DeleteUserRequestData selfRequest = new DeleteUserRequestData(
            "Bearer admin.token", "admin-123" // ID mục tiêu = ID của Admin
        );
        
        ArgumentCaptor<DeleteUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(DeleteUserResponseData.class);

        // --- ACT ---
        useCase.execute(selfRequest);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        DeleteUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Admin không thể tự xóa chính mình.", presentedResponse.message);
        
        verify(mockUserRepository, never()).findByUserId(anyString());
    }
    
    /**
     * Test kịch bản THẤT BẠI: Lỗi hệ thống (CSDL sập khi Update)
     */
    @Test
    void test_execute_failure_systemErrorOnUpdate() {
        // --- ARRANGE ---
        when(mockTokenValidator.validate("Bearer admin.token")).thenReturn(adminPrincipal);
        when(mockUserRepository.findByUserId("user-456")).thenReturn(targetUserData);
        
        // 1. Giả lập CSDL sập khi UPDATE
        when(mockUserRepository.update(any(UserData.class)))
            .thenThrow(new RuntimeException("Database connection failed"));
            
        ArgumentCaptor<DeleteUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(DeleteUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        verify(mockUserRepository).update(any(UserData.class)); // Đã cố gọi update
        
        verify(mockOutputBoundary).present(responseCaptor.capture());
        DeleteUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Đã xảy ra lỗi hệ thống không xác định.", presentedResponse.message);
    }
}
