package Category.DeleteCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryOutputBoundary;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryRequestData;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryResponseData;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryUseCase;
import cgx.com.usecase.ManageUser.IUserRepository;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCategoryUseCaseTest {

    @Mock private ICategoryRepository categoryRepository;
    @Mock private IUserRepository userRepository; 
    @Mock private IAuthTokenValidator tokenValidator;
    @Mock private DeleteCategoryOutputBoundary outputBoundary;

    private DeleteCategoryUseCase deleteCategoryUseCase;

    @BeforeEach
    void setUp() {
        deleteCategoryUseCase = new DeleteCategoryUseCase(
                categoryRepository, tokenValidator, userRepository, outputBoundary
        );
    }

    @Test
    @DisplayName("Fail: User không phải Admin")
    void execute_ShouldFail_WhenUserIsNotAdmin() {
        // Arrange
        DeleteCategoryRequestData input = new DeleteCategoryRequestData();
        input.authToken = "customer-token";

        AuthPrincipal customerPrincipal = new AuthPrincipal("u1", "email", UserRole.CUSTOMER);
        when(tokenValidator.validate(input.authToken)).thenReturn(customerPrincipal);

        // Act
        deleteCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        DeleteCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Không có quyền truy cập.", output.message);
    }
    
    @Test
    @DisplayName("Fail: ID danh mục rỗng")
    void execute_ShouldFail_WhenCategoryIdIsInvalid() {
        // Arrange
        DeleteCategoryRequestData input = new DeleteCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = ""; 

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        deleteCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        DeleteCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("ID danh mục không được để trống.", output.message);
    }

    @Test
    @DisplayName("Fail: Danh mục không tồn tại trong DB")
    void execute_ShouldFail_WhenCategoryNotFound() {
        // Arrange
        DeleteCategoryRequestData input = new DeleteCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = "unknown-id";

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);
        
        // Mock repository trả về null
        when(categoryRepository.findById(input.categoryId)).thenReturn(null);

        // Act
        deleteCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        DeleteCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        // Message từ
        assertEquals("Không tìm thấy danh mục với ID: " + input.categoryId, output.message);
    }

    @Test
    @DisplayName("Fail: Danh mục đang chứa danh mục con")
    void execute_ShouldFail_WhenCategoryHasChildren() {
        // Arrange
        DeleteCategoryRequestData input = new DeleteCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = "parent-cat";

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Mock tồn tại danh mục
        CategoryData existingData = new CategoryData("parent-cat", "Parent", "", null, null, null);
        when(categoryRepository.findById(input.categoryId)).thenReturn(existingData);

        // Mock có con -> trả về true
        when(categoryRepository.hasChildren(input.categoryId)).thenReturn(true);

        // Act
        deleteCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        DeleteCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        // Message từ
        assertEquals("Không thể xóa danh mục này vì nó đang chứa các danh mục con.", output.message);
    }

    @Test
    @DisplayName("Fail: Danh mục đang chứa sản phẩm (Products)")
    void execute_ShouldFail_WhenCategoryHasProducts() {
        // Arrange
        DeleteCategoryRequestData input = new DeleteCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = "cat-products";

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        CategoryData existingData = new CategoryData("cat-products", "Cat", "", null, null, null);
        when(categoryRepository.findById(input.categoryId)).thenReturn(existingData);

        when(categoryRepository.hasChildren(input.categoryId)).thenReturn(false);
        when(categoryRepository.hasProducts(input.categoryId)).thenReturn(true);

        // Act
        deleteCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        DeleteCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Không thể xóa danh mục này vì nó đang chứa sản phẩm.", output.message);
    }

    @Test
    @DisplayName("Success: Xóa danh mục thành công")
    void execute_ShouldSucceed_WhenAllConditionsMet() {
        // Arrange
        DeleteCategoryRequestData input = new DeleteCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = "valid-cat-id";

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // 1. Tồn tại
        CategoryData existingData = new CategoryData("valid-cat-id", "Cat", "", null, null, null);
        when(categoryRepository.findById(input.categoryId)).thenReturn(existingData);

        when(categoryRepository.hasChildren(input.categoryId)).thenReturn(false);
        when(categoryRepository.hasProducts(input.categoryId)).thenReturn(false);

        // Act
        deleteCategoryUseCase.execute(input);

        // Assert Output
        ArgumentCaptor<DeleteCategoryResponseData> captor = ArgumentCaptor.forClass(DeleteCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        DeleteCategoryResponseData output = captor.getValue();

        assertTrue(output.success);
        assertEquals("Xóa danh mục thành công.", output.message);
        assertEquals("valid-cat-id", output.deletedCategoryId);
        verify(categoryRepository, times(1)).delete(input.categoryId);
    }
}