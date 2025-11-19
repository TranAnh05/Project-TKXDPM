package User.ViewUserProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewOwnProfileUseCase;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileOutputBoundary;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileRequestData;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

@ExtendWith(MockitoExtension.class)
public class ViewOwnProfileUseCaseTest {
	// 1. Mock các dependencies
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IUserRepository mockUserRepository;
    @Mock private ViewUserProfileOutputBoundary mockOutputBoundary;

    // 2. Lớp cần test
    private ViewOwnProfileUseCase useCase;

    // 3. Dữ liệu mẫu
    private ViewUserProfileRequestData requestData;
    private AuthPrincipal validPrincipal;
    private UserData validUserData;
    
    @BeforeEach
    void setUp() {
        useCase = new ViewOwnProfileUseCase(
            mockTokenValidator,
            mockUserRepository,
            mockOutputBoundary
        );

        requestData = new ViewUserProfileRequestData("Bearer jwt.token.string");
        
        validPrincipal = new AuthPrincipal("user-123", "test@example.com", UserRole.CUSTOMER);

        validUserData = new UserData();
        validUserData.userId = "user-123";
        validUserData.email = "test@example.com";
        validUserData.firstName = "John";
        validUserData.lastName = "Doe";
        validUserData.phoneNumber = "0909123456";
    }
    
    /**
     * Test kịch bản THÀNH CÔNG
     */
    @Test
    void test_execute_success() {
        // --- ARRANGE ---
        // 1. Giả lập Token hợp lệ
        when(mockTokenValidator.validate("Bearer jwt.token.string")).thenReturn(validPrincipal);
        // 2. Giả lập tìm thấy User
        when(mockUserRepository.findByUserId("user-123")).thenReturn(validUserData);
        
        ArgumentCaptor<ViewUserProfileResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ViewUserProfileResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        verify(mockTokenValidator).validate("Bearer jwt.token.string");
        verify(mockUserRepository).findByUserId("user-123");
        
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ViewUserProfileResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertEquals("Lấy thông tin thành công.", presentedResponse.message);
        assertEquals("user-123", presentedResponse.userId);
        assertEquals("John", presentedResponse.firstName);
        assertEquals("0909123456", presentedResponse.phoneNumber);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Token không hợp lệ
     */
    @Test
    void test_execute_failure_invalidToken() {
        // --- ARRANGE ---
        // 1. Giả lập Token KHÔNG hợp lệ
        when(mockTokenValidator.validate("Bearer invalid.token"))
            .thenThrow(new SecurityException("Token đã hết hạn."));
            
        ArgumentCaptor<ViewUserProfileResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ViewUserProfileResponseData.class);

        // --- ACT ---
        useCase.execute(new ViewUserProfileRequestData("Bearer invalid.token"));
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ViewUserProfileResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Token đã hết hạn.", presentedResponse.message);
        
        // Quan trọng: Không bao giờ gọi CSDL
        verify(mockUserRepository, never()).findByUserId(anyString());
    }
    
    /**
     * Test kịch bản THẤT BẠI: Token hợp lệ, nhưng User không tồn tại
     */
    @Test
    void test_execute_failure_userNotFound() {
        // --- ARRANGE ---
        // 1. Giả lập Token hợp lệ
        when(mockTokenValidator.validate("Bearer jwt.token.string")).thenReturn(validPrincipal);
        // 2. Giả lập KHÔNG tìm thấy User
        when(mockUserRepository.findByUserId("user-123")).thenReturn(null);

        ArgumentCaptor<ViewUserProfileResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ViewUserProfileResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        verify(mockTokenValidator).validate("Bearer jwt.token.string");
        verify(mockUserRepository).findByUserId("user-123");
        
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ViewUserProfileResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Không tìm thấy người dùng.", presentedResponse.message);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Token bị để trống
     */
    @Test
    void test_execute_failure_tokenEmpty() {
        // --- ARRANGE ---
        ArgumentCaptor<ViewUserProfileResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ViewUserProfileResponseData.class);

        // --- ACT ---
        useCase.execute(new ViewUserProfileRequestData("")); // Token rỗng
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ViewUserProfileResponseData presentedResponse = responseCaptor.getValue();
        
        assertFalse(presentedResponse.success);
        assertEquals("Auth Token không được để trống.", presentedResponse.message);
        
        // Dừng ngay từ đầu, không gọi validator hay CSDL
        verify(mockTokenValidator, never()).validate(anyString());
        verify(mockUserRepository, never()).findByUserId(anyString());
    }
    
    /**
     * Test kịch bản THẤT BẠI: csdl sập
     */
    @Test
    void test_execute_failure_DBFallOff() {
        // --- ARRANGE ---
    	 when(mockTokenValidator.validate("Bearer jwt.token.string")).thenReturn(validPrincipal);
    	when(mockUserRepository.findByUserId("user-123")).thenThrow(new RuntimeException("csdl sập"));
        ArgumentCaptor<ViewUserProfileResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ViewUserProfileResponseData.class);

        // --- ACT ---
        useCase.execute(new ViewUserProfileRequestData("Bearer jwt.token.string")); // Token rỗng
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ViewUserProfileResponseData presentedResponse = responseCaptor.getValue();
        
        assertFalse(presentedResponse.success);
        assertEquals("Đã xảy ra lỗi hệ thống không xác định.", presentedResponse.message);
    }
}
