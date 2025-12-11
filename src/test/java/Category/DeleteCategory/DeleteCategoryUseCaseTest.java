package Category.DeleteCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryOutputBoundary;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryRequestData;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryResponseData;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryUseCase;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteCategoryUseCaseTest {

    @Mock private ICategoryRepository mockCategoryRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private DeleteCategoryOutputBoundary mockOutputBoundary;

    private DeleteCategoryUseCase useCase;
    private AuthPrincipal adminPrincipal;
    private CategoryData existingCategory;

    @BeforeEach
    void setUp() {
        useCase = new DeleteCategoryUseCase(mockCategoryRepository, mockTokenValidator, mockOutputBoundary);
        
        // Valid Admin
        adminPrincipal = new AuthPrincipal("admin-1", "admin@test.com", UserRole.ADMIN);
        
        // Existing Category Data
        existingCategory = new CategoryData("cat-1", "Name", "Desc", null, Instant.now(), Instant.now());
    }

    /**
     * Case 1: Success - Delete valid category (No children, No products)
     */
    @Test
    void test_execute_success() {
        // ARRANGE
        DeleteCategoryRequestData input = new DeleteCategoryRequestData("token", "cat-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-1")).thenReturn(existingCategory);
        when(mockCategoryRepository.hasChildren("cat-1")).thenReturn(false);
        when(mockCategoryRepository.hasProducts("cat-1")).thenReturn(false);

        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository).delete("cat-1");
        verify(mockOutputBoundary).present(captor.capture());
        
        assertTrue(captor.getValue().success);
        assertEquals("Xóa danh mục thành công.", captor.getValue().message);
        assertEquals("cat-1", captor.getValue().deletedCategoryId);
    }

    /**
     * Case 2: Failure - Business Rule: Has Children
     */
    @Test
    void test_execute_failure_hasChildren() {
        // ARRANGE
        DeleteCategoryRequestData input = new DeleteCategoryRequestData("token", "cat-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-1")).thenReturn(existingCategory);
        when(mockCategoryRepository.hasChildren("cat-1")).thenReturn(true); // Has children

        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        
        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository, never()).delete(anyString()); // Should NOT delete
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không thể xóa danh mục này vì nó đang chứa các danh mục con.", captor.getValue().message);
    }

    /**
     * Case 3: Failure - Business Rule: Has Products
     */
    @Test
    void test_execute_failure_hasProducts() {
        // ARRANGE
        DeleteCategoryRequestData input = new DeleteCategoryRequestData("token", "cat-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-1")).thenReturn(existingCategory);
        when(mockCategoryRepository.hasChildren("cat-1")).thenReturn(false);
        when(mockCategoryRepository.hasProducts("cat-1")).thenReturn(true); // Has products

        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        
        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository, never()).delete(anyString());
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không thể xóa danh mục này vì nó đang chứa sản phẩm.", captor.getValue().message);
    }

    /**
     * Case 4: Failure - Category Not Found
     */
    @Test
    void test_execute_failure_notFound() {
        // ARRANGE
        DeleteCategoryRequestData input = new DeleteCategoryRequestData("token", "cat-999");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-999")).thenReturn(null); // Not found

        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        
        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository, never()).delete(anyString());
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy danh mục với ID: cat-999", captor.getValue().message);
    }
    
    /**
     * Case 5: Failure - Auth Token Empty (Input Validation)
     */
    @Test
    void test_execute_failure_emptyToken() {
        // ARRANGE
        DeleteCategoryRequestData input = new DeleteCategoryRequestData("", "cat-1");
        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        
        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockTokenValidator, never()).validate(anyString());
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Auth Token không được để trống.", captor.getValue().message);
    }
    
    /**
     * Case 6: Failure - Category ID Empty (Input Validation)
     */
    @Test
    void test_execute_failure_emptyCategoryId() {
        // ARRANGE
        DeleteCategoryRequestData input = new DeleteCategoryRequestData("token", "");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        
        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository, never()).findById(anyString());
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("ID danh mục không được để trống.", captor.getValue().message);
    }
    
    /**
     * Case 7: Failure - Not Admin (Authorization)
     */
    @Test
    void test_execute_failure_notAdmin() {
        // ARRANGE
        DeleteCategoryRequestData input = new DeleteCategoryRequestData("token", "cat-1");
        AuthPrincipal customer = new AuthPrincipal("u1", "e@e.com", UserRole.CUSTOMER);
        
        when(mockTokenValidator.validate("token")).thenReturn(customer);
        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        
        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository, never()).findById(anyString());
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không có quyền truy cập (Yêu cầu Admin).", captor.getValue().message);
    }

    /**
     * Case 8: Failure - Database Broken (System Error)
     */
    @Test
    void test_execute_failure_dbCrash() {
        // ARRANGE
        DeleteCategoryRequestData input = new DeleteCategoryRequestData("token", "cat-1");
        
        // Setup conditions to reach delete() method
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-1")).thenReturn(existingCategory);
        when(mockCategoryRepository.hasChildren("cat-1")).thenReturn(false);
        when(mockCategoryRepository.hasProducts("cat-1")).thenReturn(false);
        
        // SIMULATE DB CRASH
        doThrow(new RuntimeException("DB Connection Failed")).when(mockCategoryRepository).delete("cat-1");

        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        
        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository).delete("cat-1"); // Attempted delete
        verify(mockOutputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Lỗi hệ thống không xác định.", captor.getValue().message);
    }
}