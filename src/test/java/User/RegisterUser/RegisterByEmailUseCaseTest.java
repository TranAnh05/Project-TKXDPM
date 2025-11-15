package User.RegisterUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import usecase.ManageUser.IPasswordHasher;
import usecase.ManageUser.IUserIdGenerator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;
import usecase.ManageUser.RegisterUser.RegisterByEmailUseCase;
import usecase.ManageUser.RegisterUser.RegisterUserOutputBoundary;
import usecase.ManageUser.RegisterUser.RegisterUserRequestData;
import usecase.ManageUser.RegisterUser.RegisterUserResponseData;

/**
 * Unit Test cho Use Case (Interactor) - Layer 3
 * Chúng ta test RegisterByEmailUseCase (lớp Con)
 * nhưng thực chất là đang test logic của AbstractRegisterUserUseCase (lớp Cha).
 */
@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito
public class RegisterByEmailUseCaseTest {
	// 1. Mock tất cả các dependencies (Interfaces/Ports)
    @Mock
    private IUserRepository mockUserRepository;
    @Mock
    private IPasswordHasher mockPasswordHasher;
    @Mock
    private IUserIdGenerator mockUserIdGenerator;
    @Mock
    private RegisterUserOutputBoundary mockOutputBoundary; // Mock Presenter

    // 2. Lớp cần test
    private RegisterByEmailUseCase useCase;
    
    // 3. Dữ liệu đầu vào mẫu
    private RegisterUserRequestData requestData;
    
    @BeforeEach
    void setUp() {
        // Khởi tạo lớp cần test và inject các mock
        useCase = new RegisterByEmailUseCase(
            mockUserRepository,
            mockPasswordHasher,
            mockUserIdGenerator,
            mockOutputBoundary
        );
        
        // Dữ liệu mẫu cho các test
        requestData = new RegisterUserRequestData(
            "test@example.com",
            "password123",
            "John",
            "Doe"
        );
    }

    /**
     * Test kịch bản THÀNH CÔNG (Happy Path)
     */
    @Test
    void test_execute_success() {
        // --- ARRANGE (Thiết lập Mock) ---
        
        // 1. Giả lập email chưa tồn tại
        when(mockUserRepository.findByEmail("test@example.com")).thenReturn(null);
        
        // 2. Giả lập ID Generator
        when(mockUserIdGenerator.generate()).thenReturn("user-uuid-123");
        
        // 3. Giả lập Password Hasher
        when(mockPasswordHasher.hash("password123")).thenReturn("hashed_password_abc");

        // 4. Giả lập việc lưu CSDL thành công
        UserData savedUserData = new UserData(
            "user-uuid-123", "test@example.com", "hashed_password_abc",
            "John", "Doe", null, null, null, Instant.now(), Instant.now()
        );
        when(mockUserRepository.save(any(UserData.class))).thenReturn(savedUserData);

        // 5. Chuẩn bị "bắt" dữ liệu đầu ra mà Use Case gửi cho Presenter
        ArgumentCaptor<RegisterUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(RegisterUserResponseData.class);

        // --- ACT (Thực thi) ---
        useCase.execute(requestData);

        // --- ASSERT (Kiểm chứng) ---
        
        // 1. Kiểm tra các hàm đã được gọi đúng
        verify(mockUserRepository).findByEmail("test@example.com"); // Đã kiểm tra email
        verify(mockUserIdGenerator).generate(); // Đã tạo ID
        verify(mockPasswordHasher).hash("password123"); // Đã băm pass
        verify(mockUserRepository).save(any(UserData.class)); // Đã lưu CSDL
        
        // 2. Kiểm tra dữ liệu đã được gửi đến Presenter
        verify(mockOutputBoundary).present(responseCaptor.capture());
        
        RegisterUserResponseData presentedResponse = responseCaptor.getValue();
        
        assertTrue(presentedResponse.success);
        assertEquals("Đăng ký tài khoản thành công!", presentedResponse.message);
        assertEquals("user-uuid-123", presentedResponse.createdUserId);
        assertEquals("test@example.com", presentedResponse.email);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Email đã tồn tại
     */
    @Test
    void test_execute_failure_emailAlreadyExists() {
        // --- ARRANGE ---
        // 1. Giả lập email ĐÃ TỒN TẠI
        when(mockUserRepository.findByEmail("test@example.com")).thenReturn(new UserData());
        
        ArgumentCaptor<RegisterUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(RegisterUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        
        // 1. Kiểm tra: Dừng lại ngay sau khi check email
        verify(mockUserRepository).findByEmail("test@example.com");
        // 2. Quan trọng: Các hàm sau KHÔNG BAO GIỜ được gọi
        verify(mockUserIdGenerator, never()).generate();
        verify(mockPasswordHasher, never()).hash(toString());
        verify(mockUserRepository, never()).save(any(UserData.class));
        
        // 3. Kiểm tra response lỗi gửi cho Presenter
        verify(mockOutputBoundary).present(responseCaptor.capture());
        RegisterUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Email này đã tồn tại.", presentedResponse.message);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Mật khẩu quá ngắn (Lỗi Validation từ Entity)
     */
    @Test
    void test_execute_failure_passwordTooShort() {
        // --- ARRANGE ---
        RegisterUserRequestData badRequest = new RegisterUserRequestData(
            "test@example.com", "123", "John", "Doe" // Mật khẩu "123" quá ngắn
        );
        ArgumentCaptor<RegisterUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(RegisterUserResponseData.class);

        // --- ACT ---
        useCase.execute(badRequest);

        // --- ASSERT ---
        // 1. Kiểm tra: KHÔNG BAO GIỜ được gọi
        verify(mockUserRepository, never()).findByEmail(anyString()); // Dừng ở bước 2 (Validate)
        verify(mockUserRepository, never()).save(any(UserData.class));

        // 2. Kiểm tra response lỗi
        verify(mockOutputBoundary).present(responseCaptor.capture());
        RegisterUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Mật khẩu phải có ít nhất 8 ký tự.", presentedResponse.message);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Tên để trống (Lỗi Validation từ Entity)
     */
    @Test
    void test_execute_failure_nameIsEmpty() {
        // --- ARRANGE ---
        RegisterUserRequestData badRequest = new RegisterUserRequestData(
            "test@example.com", "password123", "", "Doe" // Tên (firstName) bị rỗng
        );
        ArgumentCaptor<RegisterUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(RegisterUserResponseData.class);
            
        // --- ACT ---
        useCase.execute(badRequest);

        // --- ASSERT ---
        verify(mockUserRepository, never()).save(any(UserData.class)); // Không bao giờ lưu
        
        verify(mockOutputBoundary).present(responseCaptor.capture());
        RegisterUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Tên không được để trống.", presentedResponse.message);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Lỗi hệ thống (ví dụ: CSDL sập)
     */
    @Test
    void test_execute_failure_systemErrorOnSave() {
        // --- ARRANGE ---
        // 1. Các bước validation và check email đều OK
        when(mockUserRepository.findByEmail("test@example.com")).thenReturn(null);
        when(mockUserIdGenerator.generate()).thenReturn("user-uuid-123");
        when(mockPasswordHasher.hash("password123")).thenReturn("hashed_password_abc");
        
        // 2. Giả lập CSDL bị sập khi SAVE
        when(mockUserRepository.save(any(UserData.class)))
            .thenThrow(new RuntimeException("Database connection failed"));
            
        ArgumentCaptor<RegisterUserResponseData> responseCaptor = 
            ArgumentCaptor.forClass(RegisterUserResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Đã cố gắng gọi save
        verify(mockUserRepository).save(any(UserData.class));
        
        // 2. Kiểm tra response lỗi hệ thống
        verify(mockOutputBoundary).present(responseCaptor.capture());
        RegisterUserResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Đã xảy ra lỗi hệ thống không xác định. Vui lòng thử lại sau.", presentedResponse.message);
    }
}
