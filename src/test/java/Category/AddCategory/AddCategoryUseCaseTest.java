package Category.AddCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryIdGenerator;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryOutputBoundary;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryRequestData;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryResponseData;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryUseCase;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.*;
import cgx.com.usecase.ManageCategory.AddNewCategory.*;

@ExtendWith(MockitoExtension.class)
public class AddCategoryUseCaseTest {
	@Mock private ICategoryRepository mockCategoryRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private ICategoryIdGenerator mockIdGenerator;
    @Mock private AddCategoryOutputBoundary mockOutputBoundary;

    private AddCategoryUseCase useCase;
    private AuthPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        useCase = new AddCategoryUseCase(
            mockCategoryRepository, mockTokenValidator, mockIdGenerator, mockOutputBoundary
        );
        adminPrincipal = new AuthPrincipal("admin-1", "admin@test.com", UserRole.ADMIN);
    }
    
    /**
     * Trường hợp thành công
     * */
    @Test
    void test_execute_success_rootCategory() {
        // ARRANGE
        AddCategoryRequestData input = new AddCategoryRequestData("token", "Laptop", "Desc", null);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findByName("Laptop")).thenReturn(null); // Chưa tồn tại
        when(mockIdGenerator.generate()).thenReturn("cat-1");

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository).save(any(CategoryData.class));
        verify(mockOutputBoundary).present(captor.capture());
        
        AddCategoryResponseData output = captor.getValue();
        assertTrue(output.success);
        assertEquals("Laptop", output.name);
        assertNull(output.parentCategoryId);
    }
    
    
    /**
     * Trường hợp thành công - danh mục cha tồn tại
     * */
    @Test
    void test_execute_success_childCategory() {
        // ARRANGE
        // Tạo danh mục con "Gaming Laptop" thuộc cha "cat-1"
        AddCategoryRequestData input = new AddCategoryRequestData("token", "Gaming Laptop", "Desc", "cat-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findByName("Gaming Laptop")).thenReturn(null);
        // Giả lập danh mục cha CÓ tồn tại
        when(mockCategoryRepository.findById("cat-1")).thenReturn(new CategoryData()); 
        when(mockIdGenerator.generate()).thenReturn("cat-2");

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockOutputBoundary).present(captor.capture());
        assertTrue(captor.getValue().success);
        assertEquals("cat-1", captor.getValue().parentCategoryId);
    }
    
    /**
     * thất bại - tên danh mục tồn tại
     * */
    @Test
    void test_execute_failure_duplicateName() {
        // ARRANGE
        AddCategoryRequestData input = new AddCategoryRequestData("token", "Laptop", "Desc", null);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        // Giả lập tên ĐÃ tồn tại
        when(mockCategoryRepository.findByName("Laptop")).thenReturn(new CategoryData());

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository, never()).save(any());
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Tên danh mục đã tồn tại: Laptop", captor.getValue().message);
    }
    
    /**
     * thất bại - danh mục cha không tồn tại
     * */
    @Test
    void test_execute_failure_parentNotFound() {
        // ARRANGE
        AddCategoryRequestData input = new AddCategoryRequestData("token", "Sub", "Desc", "invalid-parent");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findByName("Sub")).thenReturn(null);
        // Giả lập cha KHÔNG tồn tại
        when(mockCategoryRepository.findById("invalid-parent")).thenReturn(null);

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository, never()).save(any());
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Danh mục cha không tồn tại.", captor.getValue().message);
    }
    
    /**
     * Case 5: Thất bại - Token bị rỗng (Input Validation)
     */
    @Test
    void test_execute_failure_emptyToken() {
        // ARRANGE
        AddCategoryRequestData input = new AddCategoryRequestData("", "Laptop", "Desc", null);
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockTokenValidator, never()).validate(anyString());
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Auth Token không được để trống.", captor.getValue().message);
    }
    
    /**
     * Case 6: Thất bại - Không phải Admin (Authorization)
     */
    @Test
    void test_execute_failure_notAdmin() {
        // ARRANGE
        AddCategoryRequestData input = new AddCategoryRequestData("token", "Laptop", "Desc", null);
        AuthPrincipal customerPrincipal = new AuthPrincipal("cust-1", "cust@test.com", UserRole.CUSTOMER);
        
        when(mockTokenValidator.validate("token")).thenReturn(customerPrincipal);
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository, never()).findByName(anyString());
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không có quyền truy cập (Yêu cầu Admin).", captor.getValue().message);
    }

    /**
     * Case 7: Thất bại - Tên danh mục không hợp lệ (Validation from Entity)
     */
    @Test
    void test_execute_failure_invalidName() {
        // ARRANGE
        AddCategoryRequestData input = new AddCategoryRequestData("token", "A", "Desc", null); // Quá ngắn (< 2 ký tự)
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository, never()).findByName(anyString());
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        // Message này đến từ Entity Category.validateName
        assertEquals("Tên danh mục phải có ít nhất 2 ký tự.", captor.getValue().message);
    }

    /**
     * Case 8: Thất bại - Lỗi hệ thống / Database hỏng (System Error)
     */
    @Test
    void test_execute_failure_databaseBroken() {
        // ARRANGE
        AddCategoryRequestData input = new AddCategoryRequestData("token", "Laptop", "Desc", null);
        
        // Mọi điều kiện validation đều OK
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findByName("Laptop")).thenReturn(null);
        when(mockIdGenerator.generate()).thenReturn("cat-1");
        
        // GIẢ LẬP LỖI: Database ném Exception khi gọi save()
        doThrow(new RuntimeException("DB Connection Failed")).when(mockCategoryRepository).save(any(CategoryData.class));

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository).save(any(CategoryData.class)); // Đã cố gắng lưu
        
        verify(mockOutputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();
        
        // Kiểm tra phản hồi lỗi hệ thống
        assertFalse(output.success);
        assertEquals("Lỗi hệ thống không xác định.", output.message);
    }
}
