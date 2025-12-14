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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.*;
import cgx.com.usecase.ManageCategory.AddNewCategory.*;

@ExtendWith(MockitoExtension.class)
class AddCategoryUseCaseTest {

    @Mock private ICategoryRepository categoryRepository;
    @Mock private IAuthTokenValidator tokenValidator;
    @Mock private ICategoryIdGenerator idGenerator;
    @Mock private AddCategoryOutputBoundary outputBoundary;

    private AddCategoryUseCase addCategoryUseCase;

    @BeforeEach
    void setUp() {
        // Khởi tạo UseCase với các mock dependency
        addCategoryUseCase = new AddCategoryUseCase(
                categoryRepository, tokenValidator, idGenerator, outputBoundary
        );
    }
    
    @Test
    @DisplayName("Fail: User không phải Admin")
    void execute_ShouldFail_WhenUserIsNotAdmin() {
        // Arrange
        AddCategoryRequestData input = new AddCategoryRequestData();
        input.authToken = "customer-token";

        AuthPrincipal customerPrincipal = new AuthPrincipal("u1", "test@mail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(input.authToken)).thenReturn(customerPrincipal);

        // Act
        addCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Không có quyền truy cập.", output.message);
    }

    @Test
    @DisplayName("3. Fail: Tên danh mục rỗng")
    void execute_ShouldFail_WhenCategoryNameIsInvalid() {
        // Arrange
        AddCategoryRequestData input = new AddCategoryRequestData();
        input.authToken = "admin-token";
        input.name = ""; 

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        addCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Tên danh mục không được để trống.", output.message);
    }
    
    @Test
    @DisplayName("4. Fail: Tên danh mục bị trùng ")
    void execute_ShouldFail_WhenCategoryNameAlreadyExists() {
        // Arrange
        AddCategoryRequestData input = new AddCategoryRequestData();
        input.authToken = "admin-token";
        input.name = "Electronics";

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        CategoryData existingCategory = new CategoryData("id1", "Electronics", "desc", null, Instant.now(), Instant.now());
        when(categoryRepository.findByName(input.name)).thenReturn(existingCategory);

        // Act
        addCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Tên danh mục đã tồn tại: " + input.name, output.message);
    }
    
    @Test
    @DisplayName("5. Fail: Danh mục cha (ParentID) không tồn tại")
    void execute_ShouldFail_WhenParentCategoryNotFound() {
        // Arrange
        AddCategoryRequestData input = new AddCategoryRequestData();
        input.authToken = "admin-token";
        input.name = "Laptop";
        input.parentCategoryId = "invalid-parent-id";

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);
        
        when(categoryRepository.findByName(input.name)).thenReturn(null);
        
        when(categoryRepository.findById(input.parentCategoryId)).thenReturn(null);

        // Act
        addCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Danh mục cha không tồn tại.", output.message);
    }
    
    @Test
    @DisplayName("6. Success: Tạo danh mục thành công")
    void execute_ShouldSucceed_WhenAllInputsAreValid() {
        // Arrange
        AddCategoryRequestData input = new AddCategoryRequestData();
        input.authToken = "admin-token";
        input.name = "Smartphones";
        input.description = "New phones";
        input.parentCategoryId = "electronics-id"; 

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin@mail.com", UserRole.ADMIN);
        
        // 1. Validate Token OK
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);
        
        
        when(categoryRepository.findByName(input.name)).thenReturn(null);
        
        CategoryData parentCategory = new CategoryData("electronics-id", "Elec", "", null, Instant.now(), Instant.now());
        when(categoryRepository.findById(input.parentCategoryId)).thenReturn(parentCategory);

        when(idGenerator.generate()).thenReturn("new-cat-id");

        // Act
        addCategoryUseCase.execute(input);

        // Assert Output
        ArgumentCaptor<AddCategoryResponseData> outputCaptor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(outputCaptor.capture());
        AddCategoryResponseData output = outputCaptor.getValue();

        assertTrue(output.success);
        assertEquals("Thêm danh mục thành công.", output.message); //
        assertEquals("new-cat-id", output.categoryId);
        assertEquals("Smartphones", output.name);

        ArgumentCaptor<CategoryData> saveCaptor = ArgumentCaptor.forClass(CategoryData.class);
        verify(categoryRepository).save(saveCaptor.capture());
        CategoryData savedData = saveCaptor.getValue();

        assertEquals("new-cat-id", savedData.categoryId);
        assertEquals("Smartphones", savedData.name);
        assertEquals("electronics-id", savedData.parentCategoryId);
    }
}