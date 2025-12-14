package Category.UpdateCategory;

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
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryResponseData;
import cgx.com.usecase.ManageCategory.UpdateCategory.UpdateCategoryOutputBoundary;
import cgx.com.usecase.ManageCategory.UpdateCategory.UpdateCategoryRequestData;
import cgx.com.usecase.ManageCategory.UpdateCategory.UpdateCategoryUseCase;
import cgx.com.usecase.ManageUser.IUserRepository;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryUseCaseTest {

    @Mock private ICategoryRepository categoryRepository;
    @Mock private IAuthTokenValidator tokenValidator;
    @Mock private UpdateCategoryOutputBoundary outputBoundary;

    private UpdateCategoryUseCase updateCategoryUseCase;

    @BeforeEach
    void setUp() {
        updateCategoryUseCase = new UpdateCategoryUseCase(
            categoryRepository, tokenValidator, outputBoundary
        );
    }

    @Test
    @DisplayName("Fail: User không phải Admin ")
    void execute_ShouldFail_WhenUserIsNotAdmin() {
        // Arrange
        UpdateCategoryRequestData input = new UpdateCategoryRequestData();
        input.authToken = "customer-token";

        AuthPrincipal customerPrincipal = new AuthPrincipal("u1", "test@mail.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(input.authToken)).thenReturn(customerPrincipal);

        // Act
        updateCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Không có quyền truy cập.", output.message);
    }

    @Test
    @DisplayName("Fail: ID danh mục rỗng")
    void execute_ShouldFail_WhenCategoryIdIsEmpty() {
        // Arrange
        UpdateCategoryRequestData input = new UpdateCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = ""; // ID rỗng

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        updateCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("ID danh mục không được để trống.", output.message); //
    }
    @Test
    @DisplayName("Fail: Tên danh mục rỗng")
    void execute_ShouldFail_WhenCategoryNameIsInvalid() {
        // Arrange
        UpdateCategoryRequestData input = new UpdateCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = "valid-id";
        input.name = ""; 
        
        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Act
        updateCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        assertEquals("Tên danh mục không được để trống.", output.message); //
    }

    @Test
    @DisplayName("Fail: Không tìm thấy danh mục trong DB")
    void execute_ShouldFail_WhenCategoryNotFound() {
        // Arrange
        UpdateCategoryRequestData input = new UpdateCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = "non-existent-id";
        input.name = "Valid Name";

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);
        
        // Mock Repository trả về null
        when(categoryRepository.findById(input.categoryId)).thenReturn(null);

        // Act
        updateCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        //
        assertEquals("Không tìm thấy danh mục với ID: " + input.categoryId, output.message);
    }
    @Test
    @DisplayName("6. Fail: Tên mới trùng với danh mục khác")
    void execute_ShouldFail_WhenNewNameExists() {
        // Arrange
        UpdateCategoryRequestData input = new UpdateCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = "cat-01";
        input.name = "New Name"; // Tên muốn đổi

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Danh mục hiện tại trong DB (Tên cũ là "Old Name")
        CategoryData existingData = new CategoryData("cat-01", "Old Name", "desc", null, Instant.now(), Instant.now());
        when(categoryRepository.findById(input.categoryId)).thenReturn(existingData);

        // Mock tìm thấy một danh mục KHÁC đã dùng tên "New Name"
        CategoryData duplicateData = new CategoryData("cat-99", "New Name", "desc", null, Instant.now(), Instant.now());
        when(categoryRepository.findByName(input.name)).thenReturn(duplicateData);

        // Act
        updateCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        //
        assertEquals("Tên danh mục mới đã tồn tại: " + input.name, output.message);
    }

    @Test
    @DisplayName("Fail: Danh mục cha (ParentID) không tồn tại")
    void execute_ShouldFail_WhenParentCategoryNotFound() {
        // Arrange
        UpdateCategoryRequestData input = new UpdateCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = "cat-01";
        input.name = "Cat 01"; // Không đổi tên
        input.parentCategoryId = "invalid-parent"; // Parent ID sai

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        // Danh mục hiện tại
        CategoryData existingData = new CategoryData("cat-01", "Cat 01", "desc", null, Instant.now(), Instant.now());
        when(categoryRepository.findById(input.categoryId)).thenReturn(existingData);
        
        // Mock không tìm thấy Parent
        when(categoryRepository.findById(input.parentCategoryId)).thenReturn(null);

        // Act
        updateCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        //
        assertEquals("Danh mục cha không tồn tại.", output.message);
    }
    
    @Test
    @DisplayName("8. Fail: Chọn chính mình làm cha")
    void execute_ShouldFail_WhenParentIsSelf() {
        // Arrange
        UpdateCategoryRequestData input = new UpdateCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = "cat-01";
        input.name = "Cat 01"; 
        input.parentCategoryId = "cat-01"; // ID cha trùng ID con

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin@mail.com", UserRole.ADMIN);
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);

        CategoryData existingData = new CategoryData("cat-01", "Cat 01", "desc", null, Instant.now(), Instant.now());
        when(categoryRepository.findById(input.categoryId)).thenReturn(existingData);
        
        // Mock tìm thấy Parent (chính nó) -> để vượt qua check null
        when(categoryRepository.findById(input.parentCategoryId)).thenReturn(existingData);

        // Act
        updateCategoryUseCase.execute(input);

        // Assert
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();

        assertFalse(output.success);
        //
        assertEquals("Danh mục không thể là cha của chính nó.", output.message);
    }

    @Test
    @DisplayName("9. Success: Cập nhật thành công")
    void execute_ShouldSucceed_WhenAllInputsAreValid() {
        // Arrange
        UpdateCategoryRequestData input = new UpdateCategoryRequestData();
        input.authToken = "admin-token";
        input.categoryId = "cat-01";
        input.name = "New Name"; // Đổi tên
        input.description = "New Desc";
        input.parentCategoryId = "cat-parent"; // Đổi cha

        AuthPrincipal adminPrincipal = new AuthPrincipal("u1", "admin@mail.com", UserRole.ADMIN);
        
        // 1. Token OK
        when(tokenValidator.validate(input.authToken)).thenReturn(adminPrincipal);
        
        // 2. Existing Data OK
        CategoryData existingData = new CategoryData("cat-01", "Old Name", "Old Desc", null, Instant.now(), Instant.now());
        when(categoryRepository.findById(input.categoryId)).thenReturn(existingData);
        
        // 3. Name Duplicate Check OK (chưa có ai dùng tên "New Name")
        when(categoryRepository.findByName(input.name)).thenReturn(null);
        
        // 4. Parent Exist OK
        CategoryData parentData = new CategoryData("cat-parent", "Parent", "", null, Instant.now(), Instant.now());
        when(categoryRepository.findById(input.parentCategoryId)).thenReturn(parentData);

        // Act
        updateCategoryUseCase.execute(input);

        // Assert Output
        ArgumentCaptor<AddCategoryResponseData> outputCaptor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        verify(outputBoundary).present(outputCaptor.capture());
        AddCategoryResponseData output = outputCaptor.getValue();

        assertTrue(output.success);
        assertEquals("Cập nhật danh mục thành công.", output.message);
        assertEquals("New Name", output.name);

        // Assert Repository Save
        ArgumentCaptor<CategoryData> saveCaptor = ArgumentCaptor.forClass(CategoryData.class);
        verify(categoryRepository).save(saveCaptor.capture());
        CategoryData savedData = saveCaptor.getValue();
        
        assertEquals("cat-01", savedData.categoryId);
        assertEquals("New Name", savedData.name);
        assertEquals("cat-parent", savedData.parentCategoryId);
    }
}