package User.UpdateUserProfile;

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
import cgx.com.usecase.ManageUser.UpdateUserProfile.UpdateBasicProfileUseCase;
import cgx.com.usecase.ManageUser.UpdateUserProfile.UpdateUserProfileOutputBoundary;
import cgx.com.usecase.ManageUser.UpdateUserProfile.UpdateUserProfileRequestData;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

@ExtendWith(MockitoExtension.class)
public class UpdateBasicProfileUseCaseTest {
	// 1. Mock các dependencies
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IUserRepository mockUserRepository;
    @Mock private UpdateUserProfileOutputBoundary mockOutputBoundary;

    // 2. Lớp cần test
    private UpdateBasicProfileUseCase useCase;

    // 3. Dữ liệu mẫu
    private UpdateUserProfileRequestData requestData;
    private AuthPrincipal validPrincipal;
    private UserData existingUserData;
    

    @BeforeEach
    void setUp() {
        useCase = new UpdateBasicProfileUseCase(
            mockTokenValidator,
            mockUserRepository,
            mockOutputBoundary
        );

        // Dữ liệu MỚI người dùng gửi lên
        requestData = new UpdateUserProfileRequestData(
            "Bearer jwt.token.string",
            "Jane",         // Tên mới
            "Doe-Smith",    // Họ mới
            "0909888777"    // SĐT mới
        );
        
        validPrincipal = new AuthPrincipal("user-123", "test@example.com", UserRole.CUSTOMER);

        // Dữ liệu CŨ đang trong CSDL
        existingUserData = new UserData();
        existingUserData.userId = "user-123";
        existingUserData.email = "test@example.com";
        existingUserData.firstName = "John"; // Tên cũ
        existingUserData.lastName = "Doe";   // Họ cũ
        existingUserData.phoneNumber = null; // SĐT cũ
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
        when(mockUserRepository.findByUserId("user-123")).thenReturn(existingUserData);
        // 3. Giả lập việc Update
        // "Bắt" DTO được gửi đến CSDL
        ArgumentCaptor<UserData> userDataCaptor = ArgumentCaptor.forClass(UserData.class);
        // Giả lập hàm update trả về DTO đã bị bắt
        when(mockUserRepository.update(userDataCaptor.capture())).thenAnswer(
            invocation -> userDataCaptor.getValue()
        );
        
        ArgumentCaptor<ViewUserProfileResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ViewUserProfileResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Kiểm tra các bước
        verify(mockTokenValidator).validate("Bearer jwt.token.string");
        verify(mockUserRepository).findByUserId("user-123");
        verify(mockUserRepository).update(any(UserData.class));
        
        // 2. Kiểm tra dữ liệu GỬI ĐẾN CSDL
        UserData capturedData = userDataCaptor.getValue();
        assertEquals("Jane", capturedData.firstName);
        assertEquals("Doe-Smith", capturedData.lastName);
        assertEquals("0909888777", capturedData.phoneNumber);
        assertEquals("test@example.com", capturedData.email);
        assertNotNull(capturedData.updatedAt);
        
        // 3. Kiểm tra dữ liệu GỬI ĐẾN PRESENTER
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ViewUserProfileResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertEquals("Cập nhật hồ sơ thành công.", presentedResponse.message);
        assertEquals("Jane", presentedResponse.firstName);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Dữ liệu mới không hợp lệ (Tên rỗng)
     */
    @Test
    void test_execute_failure_invalidNewData() {
        // --- ARRANGE ---
        // Dữ liệu MỚI không hợp lệ
        UpdateUserProfileRequestData badRequest = new UpdateUserProfileRequestData(
            "Bearer jwt.token.string",
            "",         // Tên rỗng (KHÔNG hợp lệ)
            "Doe-Smith",
            "0909888777"
        );
        
        // 1. Token và User vẫn hợp lệ
        when(mockTokenValidator.validate("Bearer jwt.token.string")).thenReturn(validPrincipal);
        when(mockUserRepository.findByUserId("user-123")).thenReturn(existingUserData);

        ArgumentCaptor<ViewUserProfileResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ViewUserProfileResponseData.class);

        // --- ACT ---
        useCase.execute(badRequest);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ViewUserProfileResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Tên không được để trống.", presentedResponse.message);
        
        // Quan trọng: Không bao giờ gọi CSDL để update
        verify(mockUserRepository, never()).update(any(UserData.class));
    }
    
    /**
     * Test kịch bản THẤT BẠI: Token không hợp lệ
     */
    @Test
    void test_execute_failure_invalidToken() {
        // --- ARRANGE ---
        when(mockTokenValidator.validate(anyString()))
            .thenThrow(new SecurityException("Token đã hết hạn."));
            
        ArgumentCaptor<ViewUserProfileResponseData> responseCaptor = 
            ArgumentCaptor.forClass(ViewUserProfileResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        ViewUserProfileResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Token đã hết hạn.", presentedResponse.message);
        
        // Dừng ngay, không gọi CSDL
        verify(mockUserRepository, never()).findByUserId(anyString());
        verify(mockUserRepository, never()).update(any(UserData.class));
    }
    
    /**
     * Test kịch bản THẤT BẠI: csdl sập
     */
    @Test
    void test_execute_failure_DBFallOff() {
    	// --- ARRANGE ---

        // 1. Token hợp lệ
        when(mockTokenValidator.validate("Bearer jwt.token.string"))
                .thenReturn(validPrincipal);

        // 2. User tồn tại trong DB
        when(mockUserRepository.findByUserId("user-123"))
                .thenReturn(existingUserData);

        // 3. CSDL sập khi update()
        when(mockUserRepository.update(any(UserData.class)))
                .thenThrow(new RuntimeException("Database connection lost"));

        // Bắt dữ liệu đưa cho presenter
        ArgumentCaptor<ViewUserProfileResponseData> responseCaptor =
                ArgumentCaptor.forClass(ViewUserProfileResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---

        // 1. Các bước được gọi đúng
        verify(mockTokenValidator).validate("Bearer jwt.token.string");
        verify(mockUserRepository).findByUserId("user-123");
        verify(mockUserRepository).update(any(UserData.class));

        // 2. Presenter được gọi
        verify(mockOutputBoundary).present(responseCaptor.capture());

        ViewUserProfileResponseData output = responseCaptor.getValue();

        // 3. Kiểm tra phản hồi lỗi
        assertFalse(output.success);
        assertEquals("Đã xảy ra lỗi hệ thống không xác định.", output.message);

        // 4. Đảm bảo không cập nhật sai dữ liệu
        assertNull(output.firstName);   // Không trả dữ liệu thành công
    }
}
