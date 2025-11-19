package User.RequestPasswordReset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.usecase.ManageUser.IEmailService;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IPasswordResetTokenIdGenerator;
import cgx.com.usecase.ManageUser.IPasswordResetTokenRepository;
import cgx.com.usecase.ManageUser.ISecureTokenGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.PasswordResetTokenData;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestPasswordResetOutputBoundary;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestPasswordResetRequestData;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestPasswordResetResponseData;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestResetByEmailUseCase;

@ExtendWith(MockitoExtension.class)
public class RequestResetByEmailUseCaseTest {
	// 1. Mock tất cả các dependencies
    @Mock private IUserRepository mockUserRepository;
    @Mock private IPasswordResetTokenRepository mockTokenRepository;
    @Mock private IPasswordResetTokenIdGenerator mockTokenIdGenerator;
    @Mock private ISecureTokenGenerator mockTokenGenerator;
    @Mock private IPasswordHasher mockPasswordHasher;
    @Mock private RequestPasswordResetOutputBoundary mockOutputBoundary;
    @Mock private IEmailService mockEmailService;

    // 2. Lớp cần test
    private RequestResetByEmailUseCase useCase;

    // 3. Dữ liệu mẫu
    private RequestPasswordResetRequestData requestData;
    private UserData validUserData;
    
    @BeforeEach
    void setUp() {
        useCase = new RequestResetByEmailUseCase(
            mockUserRepository, mockTokenRepository, mockTokenIdGenerator,
            mockTokenGenerator, mockPasswordHasher, mockOutputBoundary, mockEmailService
        );

        requestData = new RequestPasswordResetRequestData("test@example.com");

        validUserData = new UserData();
        validUserData.userId = "user-123";
        validUserData.email = "test@example.com";
        validUserData.firstName = "John";
        validUserData.lastName = "Doe";
    }
    
    /**
     * Test kịch bản THÀNH CÔNG (Quan trọng): User được TÌM THẤY
     */
    @Test
    void test_execute_success_userFound() {
        // --- ARRANGE ---
        // 1. Giả lập tìm thấy User
        when(mockUserRepository.findByEmail("test@example.com")).thenReturn(validUserData);
        // 2. Giả lập các generator
        when(mockTokenIdGenerator.generate()).thenReturn("token-record-uuid-123");
        when(mockTokenGenerator.generate()).thenReturn("plain-text-token-abc");
        when(mockPasswordHasher.hash("plain-text-token-abc")).thenReturn("hashed-token-xyz");

        ArgumentCaptor<RequestPasswordResetResponseData> responseCaptor =
            ArgumentCaptor.forClass(RequestPasswordResetResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Kiểm tra các bước
        verify(mockUserRepository).findByEmail("test@example.com");
        verify(mockTokenIdGenerator).generate();
        verify(mockTokenGenerator).generate();
        verify(mockPasswordHasher).hash("plain-text-token-abc");
        verify(mockTokenRepository).save(any(PasswordResetTokenData.class));
        verify(mockEmailService).sendPasswordResetEmail(
            "test@example.com", "John Doe", "plain-text-token-abc"
        );
        
        // 2. Kiểm tra response (PHẢI là thông báo chung)
        verify(mockOutputBoundary).present(responseCaptor.capture());
        RequestPasswordResetResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertTrue(presentedResponse.message.contains("Nếu thông tin của bạn là chính xác"));
    }
    
    /**
     * Test kịch bản THÀNH CÔNG (Quan trọng): User KHÔNG TÌM THẤY
     */
    @Test
    void test_execute_success_userNotFound() {
        // --- ARRANGE ---
        // 1. Giả lập KHÔNG tìm thấy User
        when(mockUserRepository.findByEmail("test@example.com")).thenReturn(null);

        ArgumentCaptor<RequestPasswordResetResponseData> responseCaptor =
            ArgumentCaptor.forClass(RequestPasswordResetResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Chỉ check email
        verify(mockUserRepository).findByEmail("test@example.com");
        
        // 2. Quan trọng: KHÔNG BAO GIỜ được gọi các hàm "ẩn"
        verify(mockTokenGenerator, never()).generate();
        verify(mockTokenRepository, never()).save(any());
        verify(mockEmailService, never()).sendPasswordResetEmail(any(), any(), any());

        // 3. Kiểm tra response (PHẢI là thông báo chung, Y HỆT như test trên)
        verify(mockOutputBoundary).present(responseCaptor.capture());
        RequestPasswordResetResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertTrue(presentedResponse.message.contains("Nếu thông tin của bạn là chính xác"));
    }
    
    /**
     * Test kịch bản THẤT BẠI: Lỗi Validation (Email không hợp lệ)
     */
    @Test
    void test_execute_failure_invalidEmailFormat() {
        // --- ARRANGE ---
        RequestPasswordResetRequestData badRequest = 
            new RequestPasswordResetRequestData("invalid-email");
            
        ArgumentCaptor<RequestPasswordResetResponseData> responseCaptor =
            ArgumentCaptor.forClass(RequestPasswordResetResponseData.class);

        // --- ACT ---
        useCase.execute(badRequest);
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        RequestPasswordResetResponseData presentedResponse = responseCaptor.getValue();
        
        assertFalse(presentedResponse.success);
        assertEquals("Email không đúng định dạng.", presentedResponse.message);
        
        // Dừng ngay, không gọi CSDL
        verify(mockUserRepository, never()).findByEmail(anyString());
    }
    
    /**
     * Test kịch bản THẤT BẠI: Lỗi hệ thống (ví dụ: CSDL sập khi TÌM)
     */
    @Test
    void test_execute_failure_systemErrorOnFind() {
        // --- ARRANGE ---
        // 1. Giả lập CSDL sập khi TÌM
        when(mockUserRepository.findByEmail(anyString()))
            .thenThrow(new RuntimeException("Database connection failed"));
            
        ArgumentCaptor<RequestPasswordResetResponseData> responseCaptor = 
            ArgumentCaptor.forClass(RequestPasswordResetResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Đã cố gắng gọi find
        verify(mockUserRepository).findByEmail(anyString());
        
        // 2. Kiểm tra response (PHẢI là thông báo CHUNG, để che giấu lỗi hệ thống)
        verify(mockOutputBoundary).present(responseCaptor.capture());
        RequestPasswordResetResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertTrue(presentedResponse.message.contains("Nếu thông tin của bạn là chính xác"));
    }
    
    /**
     * Test kịch bản THẤT BẠI: Lỗi hệ thống (ví dụ: Dịch vụ Email sập) (MỚI)
     */
    @Test
    void test_execute_failure_emailServiceFails() {
        // --- ARRANGE ---
        // 1. Mọi thứ trước đó đều OK
        when(mockUserRepository.findByEmail("test@example.com")).thenReturn(validUserData);
        when(mockTokenIdGenerator.generate()).thenReturn("token-record-uuid-123");
        when(mockTokenGenerator.generate()).thenReturn("plain-text-token-abc");
        when(mockPasswordHasher.hash("plain-text-token-abc")).thenReturn("hashed-token-xyz");
        
        // 2. Giả lập Email Service bị sập
        doThrow(new RuntimeException("Email service is down"))
            .when(mockEmailService)
            .sendPasswordResetEmail(anyString(), anyString(), anyString());

        ArgumentCaptor<RequestPasswordResetResponseData> responseCaptor =
            ArgumentCaptor.forClass(RequestPasswordResetResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Kiểm tra đã đi qua tất cả các bước, BAO GỒM cả việc gọi email
        verify(mockUserRepository).findByEmail("test@example.com");
        verify(mockTokenRepository).save(any(PasswordResetTokenData.class));
        verify(mockEmailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        // 2. Kiểm tra response (PHẢI là thông báo CHUNG, để che giấu lỗi hệ thống)
        verify(mockOutputBoundary).present(responseCaptor.capture());
        RequestPasswordResetResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertTrue(presentedResponse.message.contains("Nếu thông tin của bạn là chính xác"));
    }
}
