package User.AdminViewUserDetails;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserResponseData;
import cgx.com.usecase.ManageUser.AdminViewUserDetails.AdminViewUserDetailsOutputBoundary;
import cgx.com.usecase.ManageUser.AdminViewUserDetails.AdminViewUserDetailsRequestData;
import cgx.com.usecase.ManageUser.AdminViewUserDetails.AdminViewUserDetailsUseCase;

@ExtendWith(MockitoExtension.class)
public class AdminViewUserDetailsUseCaseTest {

    @Mock
    private IAuthTokenValidator tokenValidator;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private AdminViewUserDetailsOutputBoundary outputBoundary;

    private AdminViewUserDetailsUseCase useCase;

    // Dữ liệu mẫu dùng chung
    private String adminToken = "valid_admin_token";
    private String adminId = "admin-001";
    private String targetUserId = "user-target-002";
    
    private AdminViewUserDetailsRequestData request;

    @BeforeEach
    void setUp() {
        useCase = new AdminViewUserDetailsUseCase(tokenValidator, userRepository, outputBoundary);

        request = new AdminViewUserDetailsRequestData();
        request.authToken = adminToken;
        request.targetUserId = targetUserId;
    }

    // --- HELPER METHODS ---
    
    private void mockAdminAuth() {
        AuthPrincipal principal = new AuthPrincipal(adminId, "admin@test.com", UserRole.ADMIN);
        when(tokenValidator.validate(adminToken)).thenReturn(principal);
    }

    // Case: Không phải admin
    @Test
    void testExecute_Fail_NotAdmin() {
        // Arrange
        // Giả lập user đăng nhập là Customer, không phải Admin
        AuthPrincipal customerPrincipal = new AuthPrincipal("cust-001", "cust@test.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(anyString())).thenReturn(customerPrincipal);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AdminUpdateUserResponseData response = captor.getValue();

        assertFalse(response.success);
        // Message định nghĩa trong User.validateIsAdmin
        assertEquals("Không có quyền truy cập.", response.message); 
    }
    
    // Case: không tìm thấy tài khoản
    @Test
    void testExecute_Fail_UserNotFound() {
        // Arrange
        mockAdminAuth(); // Đã là Admin
        
        // Giả lập DB không tìm thấy user này
        when(userRepository.findByUserId(targetUserId)).thenReturn(null);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AdminUpdateUserResponseData response = captor.getValue();

        assertFalse(response.success);
        // Message định nghĩa trong UseCase khi targetUserData == null
        assertEquals("Không tìm thấy tài khoản người dùng.", response.message);
    }

  
    // Case thành công
    @Test
    void testExecute_Success() {
        // Arrange
        mockAdminAuth();

        // Giả lập dữ liệu user tìm được từ DB
        UserData foundUser = new UserData(
            targetUserId, 
            "target@test.com", 
            "hashed_pass", 
            "Nguyen", 
            "Van B", 
            "0912345678", 
            UserRole.CUSTOMER, 
            AccountStatus.ACTIVE, 
            Instant.now(), 
            Instant.now()
        );
        when(userRepository.findByUserId(targetUserId)).thenReturn(foundUser);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AdminUpdateUserResponseData response = captor.getValue();

        // 1. Kiểm tra trạng thái
        assertTrue(response.success);
        assertEquals("Lấy thông tin thành công.", response.message);

        // 2. Kiểm tra dữ liệu được map chính xác
        assertEquals(foundUser.userId, response.userId);
        assertEquals(foundUser.email, response.email);
        assertEquals(foundUser.firstName, response.firstName);
        assertEquals(foundUser.lastName, response.lastName);
        assertEquals(foundUser.phoneNumber, response.phoneNumber);
        assertEquals(foundUser.role, response.role);
        assertEquals(foundUser.status, response.status);
    }
}
