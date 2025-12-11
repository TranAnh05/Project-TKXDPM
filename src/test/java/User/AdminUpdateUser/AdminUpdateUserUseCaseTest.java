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
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserOutputBoundary;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserRequestData;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserResponseData;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserUseCase;

@ExtendWith(MockitoExtension.class)
public class AdminUpdateUserUseCaseTest {

    @Mock
    private IAuthTokenValidator tokenValidator;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private AdminUpdateUserOutputBoundary outputBoundary;

    private AdminUpdateUserUseCase useCase;

    // Dữ liệu mẫu
    private String adminToken = "valid_admin_token";
    private String adminId = "admin-001";
    private String targetUserId = "user-002";
    
    // Request hợp lệ mặc định (để tái sử dụng trong các test case)
    private AdminUpdateUserRequestData validRequest;

    @BeforeEach
    void setUp() {
        useCase = new AdminUpdateUserUseCase(tokenValidator, userRepository, outputBoundary);

        // Khởi tạo request hợp lệ cơ bản
        validRequest = new AdminUpdateUserRequestData();
        validRequest.authToken = adminToken;
        validRequest.targetUserId = targetUserId;
        validRequest.email = "newemail@example.com";
        validRequest.firstName = "Nguyen";
        validRequest.lastName = "Van A";
        validRequest.phoneNumber = "0987654321"; 
        validRequest.role = "CUSTOMER";
        validRequest.status = "ACTIVE";
    }

    // Helper tạo Mock Admin Principal
    private void mockAdminAuth() {
        AuthPrincipal principal = new AuthPrincipal(adminId, "admin@test.com", UserRole.ADMIN);
        when(tokenValidator.validate(adminToken)).thenReturn(principal);
    }

    // Helper tạo Mock Admin Data trong DB
    private void mockAdminData() {
        UserData adminData = new UserData(adminId, "admin@test.com", "hash", "Admin", "User", "0900000000", UserRole.ADMIN, AccountStatus.ACTIVE, Instant.now(), Instant.now());
        when(userRepository.findByUserId(adminId)).thenReturn(adminData);
    }

    // Helper tạo Mock Target User Data trong DB
    private void mockTargetUserData() {
        UserData targetData = new UserData(targetUserId, "old@test.com", "hash", "Old", "Name", "0900000000", UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now(), Instant.now());
        when(userRepository.findByUserId(targetUserId)).thenReturn(targetData);
    }


    /**
     * 1. Kiểm tra Quyền Admin (User.validateIsAdmin)
     * Logic: Token hợp lệ nhưng Role là CUSTOMER -> SecurityException
     */
    @Test
    void testExecute_Fail_NotAdmin() {
        // Arrange
        AuthPrincipal customerPrincipal = new AuthPrincipal("cust-001", "cust@test.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(anyString())).thenReturn(customerPrincipal);

        // Act
        useCase.execute(validRequest);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AdminUpdateUserResponseData response = captor.getValue();

        assertFalse(response.success);
        assertEquals("Không có quyền truy cập.", response.message); 
    }

    /**
     * 2. Kiểm tra User mục tiêu tồn tại
     * Logic: Repository trả về null cho targetUserId -> SecurityException
     */
    @Test
    void testExecute_Fail_TargetNotFound() {
        // Arrange
        mockAdminAuth();
        mockAdminData();
        when(userRepository.findByUserId(targetUserId)).thenReturn(null); 
        // Act
        useCase.execute(validRequest);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy người dùng mục tiêu.", captor.getValue().message);
    }

    /**
     * 3. Kiểm tra Admin tự update chính mình (User.validateAdminSelfUpdate)
     * Logic: targetUserId == adminId -> SecurityException
     */
    @Test
    void testExecute_Fail_AdminSelfUpdate() {
        // Arrange
        mockAdminAuth();
        mockAdminData();
        // Setup để target chính là admin
        
        validRequest.targetUserId = adminId; // Request sửa chính mình

        // Act
        useCase.execute(validRequest);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Admin không thể tự"));
    }

    /**
     * 4. Kiểm tra Validation Input: Email sai định dạng (User.validateEmail)
     */
    @Test
    void testExecute_Fail_InvalidEmailFormat() {
        // Arrange
        mockAdminAuth();
        mockAdminData();
        mockTargetUserData();
        validRequest.email = "invalid-email"; // Sai format

        // Act
        useCase.execute(validRequest);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Email không đúng định dạng.", captor.getValue().message);
    }
    
    

    /**
     * 5. Kiểm tra Validation Input: Số điện thoại sai định dạng (User.validatePhoneNumber)
     */
    @Test
    void testExecute_Fail_InvalidPhoneFormat() {
        // Arrange
        mockAdminAuth();
        mockAdminData();
        mockTargetUserData();
        validRequest.phoneNumber = "12345"; // Quá ngắn, sai đầu số

        // Act
        useCase.execute(validRequest);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Số điện thoại không đúng định dạng"));
    }

    /**
     * 6. Kiểm tra Validation Input: Role không hợp lệ (User.validateRole)
     */
    @Test
    void testExecute_Fail_InvalidRole() {
        // Arrange
        mockAdminAuth();
        mockAdminData();
        mockTargetUserData();
        validRequest.role = "SUPER_GOD_MODE"; // Enum không tồn tại

        // Act
        useCase.execute(validRequest);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertTrue(captor.getValue().message.contains("Vai trò (Role) không hợp lệ"));
    }
    
    /**
     * 6a. Kiểm tra Validation Input: Tên hoặc Họ bị để trống (User.validateName)
     */
    @Test
    void testExecute_Fail_InvalidName() {
        // Arrange
        mockAdminAuth();
        mockAdminData();
        mockTargetUserData();
        
        // Trường hợp tên rỗng
        validRequest.firstName = ""; 
        validRequest.lastName = "ValidName";

        // Act
        useCase.execute(validRequest);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Message từ User.validateName: "Tên không được để trống." hoặc "Họ không được để trống."
        assertTrue(captor.getValue().message.contains("không được để trống"));
    }

    /**
     * 6b. Kiểm tra Validation Input: Trạng thái (Status) không hợp lệ (User.validateStatus)
     */
    @Test
    void testExecute_Fail_InvalidStatus() {
        // Arrange
        mockAdminAuth();
        mockAdminData();
        mockTargetUserData();
        
        // Trường hợp status không nằm trong Enum AccountStatus (VD: BLOCKED, BANNED...)
        validRequest.status = "UNKNOWN_STATUS"; 

        // Act
        useCase.execute(validRequest);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        // Message từ User.validateStatus bắt lỗi IllegalArgumentException khi gọi AccountStatus.valueOf
        System.out.println();
        assertTrue(captor.getValue().message.contains("Trạng thái (Status) không hợp lệ"));
    }

    /**
     * 7. Kiểm tra Trùng Email (Business Logic trong UseCase)
     * Logic: Email mới khác email cũ, nhưng đã tồn tại trong DB -> IllegalArgumentException
     */
    @Test
    void testExecute_Fail_DuplicateEmail() {
        // Arrange
        mockAdminAuth();
        mockAdminData();
        mockTargetUserData();
        
        String newEmail = "exist@other.com";
        validRequest.email = newEmail;

        // Mô phỏng DB tìm thấy user khác đang dùng email này
        when(userRepository.findByEmail(newEmail)).thenReturn(new UserData("other-id", newEmail, "", "", "", "", UserRole.CUSTOMER, AccountStatus.ACTIVE, null, null));

        // Act
        useCase.execute(validRequest);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Email mới này đã tồn tại.", captor.getValue().message);
    }

    @Test
    void testExecute_Success() {
        // Arrange
        mockAdminAuth();
        mockAdminData();
        mockTargetUserData();

        // Mock việc update trả về data đã update
        when(userRepository.update(any(UserData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        useCase.execute(validRequest);

        // Assert
        ArgumentCaptor<AdminUpdateUserResponseData> captor = ArgumentCaptor.forClass(AdminUpdateUserResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AdminUpdateUserResponseData response = captor.getValue();

        // 1. Kiểm tra cờ thành công
        assertTrue(response.success);
        assertEquals("Cập nhật người dùng thành công.", response.message);

        // 2. Kiểm tra dữ liệu trả về khớp request
        assertEquals(validRequest.targetUserId, response.userId);
        assertEquals(validRequest.email, response.email);
        assertEquals(validRequest.firstName, response.firstName);
        assertEquals(UserRole.valueOf(validRequest.role), response.role);
        assertEquals(AccountStatus.valueOf(validRequest.status), response.status);

        // 3. Quan trọng: Kiểm tra repository.update đã được gọi đúng tham số
        ArgumentCaptor<UserData> userCaptor = ArgumentCaptor.forClass(UserData.class);
        verify(userRepository).update(userCaptor.capture());
        UserData savedUser = userCaptor.getValue();
        
        assertEquals(validRequest.phoneNumber, savedUser.phoneNumber);
        assertEquals(validRequest.email, savedUser.email);
    }
}