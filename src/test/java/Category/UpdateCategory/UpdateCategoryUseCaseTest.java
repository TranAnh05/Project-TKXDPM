package Category.UpdateCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryResponseData;
import cgx.com.usecase.ManageCategory.UpdateCategory.UpdateCategoryOutputBoundary;
import cgx.com.usecase.ManageCategory.UpdateCategory.UpdateCategoryRequestData;
import cgx.com.usecase.ManageCategory.UpdateCategory.UpdateCategoryUseCase;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateCategoryUseCaseTest {
	@Mock private ICategoryRepository mockCategoryRepository;
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private UpdateCategoryOutputBoundary mockOutputBoundary;

    private UpdateCategoryUseCase useCase;
    private AuthPrincipal adminPrincipal;
    private CategoryData existingCategory;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCategoryUseCase(mockCategoryRepository, mockTokenValidator, mockOutputBoundary);
        
        // Valid Admin Principal
        adminPrincipal = new AuthPrincipal("admin-1", "admin@test.com", UserRole.ADMIN);
        
        // Data existing in DB
        existingCategory = new CategoryData(
            "cat-1", "Old Name", "Old Desc", null, Instant.now(), Instant.now()
        );
    }
    
    /**
     * Case 1: Success - Update Name and Description (Parent remains null)
     */
    @Test
    void test_execute_success_updateNameAndDesc() {
        // ARRANGE
        UpdateCategoryRequestData input = new UpdateCategoryRequestData(
            "token", "cat-1", "New Name", "New Desc", null
        );
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-1")).thenReturn(existingCategory);
        when(mockCategoryRepository.findByName("New Name")).thenReturn(null); // Name unique

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository).save(any(CategoryData.class));
        verify(mockOutputBoundary).present(captor.capture());
        
        AddCategoryResponseData output = captor.getValue();
        assertTrue(output.success);
        assertEquals("New Name", output.name);
        assertNull(output.parentCategoryId);
    }

    /**
     * Case 2: Success - Update Parent Category
     */
    @Test
    void test_execute_success_updateParent() {
        // ARRANGE
        UpdateCategoryRequestData input = new UpdateCategoryRequestData(
            "token", "cat-1", "Old Name", "Desc", "parent-cat-2"
        );
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-1")).thenReturn(existingCategory);
        // Name didn't change, so findByName should NOT be called for duplicate check
        // Parent exists check:
        when(mockCategoryRepository.findById("parent-cat-2")).thenReturn(new CategoryData()); 

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);

        // ACT
        useCase.execute(input);

        // ASSERT
        verify(mockCategoryRepository).save(any(CategoryData.class));
        verify(mockOutputBoundary).present(captor.capture());
        assertEquals("parent-cat-2", captor.getValue().parentCategoryId);
    }

    /**
     * Case 3: Failure - Auth Token Empty
     */
    @Test
    void test_execute_failure_emptyToken() {
        UpdateCategoryRequestData input = new UpdateCategoryRequestData("", "cat-1", "Name", "Desc", null);
        
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Auth Token không được để trống.", captor.getValue().message);
    }

    /**
     * Case 4: Failure - Not Admin
     */
    @Test
    void test_execute_failure_notAdmin() {
        UpdateCategoryRequestData input = new UpdateCategoryRequestData("token", "cat-1", "Name", "Desc", null);
        AuthPrincipal customer = new AuthPrincipal("u1", "e@e.com", UserRole.CUSTOMER);
        
        when(mockTokenValidator.validate("token")).thenReturn(customer);
        
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không có quyền truy cập (Yêu cầu Admin).", captor.getValue().message);
    }

    /**
     * Case 5: Failure - Target Category ID Empty
     */
    @Test
    void test_execute_failure_emptyCategoryId() {
        UpdateCategoryRequestData input = new UpdateCategoryRequestData("token", "", "Name", "Desc", null);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("ID danh mục không được để trống.", captor.getValue().message);
    }

    /**
     * Case 6: Failure - Category Not Found
     */
    @Test
    void test_execute_failure_notFound() {
        UpdateCategoryRequestData input = new UpdateCategoryRequestData("token", "cat-999", "Name", "Desc", null);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-999")).thenReturn(null);

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Không tìm thấy danh mục với ID: cat-999", captor.getValue().message);
    }

    /**
     * Case 7: Failure - Invalid Name (Too short) - Entity Validation
     */
    @Test
    void test_execute_failure_invalidName() {
        UpdateCategoryRequestData input = new UpdateCategoryRequestData("token", "cat-1", "A", "Desc", null);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-1")).thenReturn(existingCategory);

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Tên danh mục phải có ít nhất 2 ký tự.", captor.getValue().message);
    }

    /**
     * Case 8: Failure - Duplicate Name (Name changed and exists)
     */
    @Test
    void test_execute_failure_duplicateName() {
        UpdateCategoryRequestData input = new UpdateCategoryRequestData("token", "cat-1", "New Duplicate Name", "Desc", null);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-1")).thenReturn(existingCategory);
        // "New Duplicate Name" already exists in DB
        when(mockCategoryRepository.findByName("New Duplicate Name")).thenReturn(new CategoryData());

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Tên danh mục mới đã tồn tại: New Duplicate Name", captor.getValue().message);
    }

    /**
     * Case 9: Failure - Parent Not Found
     */
    @Test
    void test_execute_failure_parentNotFound() {
        UpdateCategoryRequestData input = new UpdateCategoryRequestData("token", "cat-1", "Old Name", "Desc", "invalid-parent");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-1")).thenReturn(existingCategory);
        when(mockCategoryRepository.findById("invalid-parent")).thenReturn(null);

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Danh mục cha không tồn tại.", captor.getValue().message);
    }

    /**
     * Case 10: Failure - Parent is Self (Loop)
     */
    @Test
    void test_execute_failure_selfParent() {
        UpdateCategoryRequestData input = new UpdateCategoryRequestData("token", "cat-1", "Name", "Desc", "cat-1");
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        // Both finding target and parent return the same object
        when(mockCategoryRepository.findById("cat-1")).thenReturn(existingCategory); 
        
        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Danh mục không thể là cha của chính nó.", captor.getValue().message);
    }

    /**
     * Case 11: Failure - Database Broken (System Error)
     */
    @Test
    void test_execute_failure_databaseBroken() {
        UpdateCategoryRequestData input = new UpdateCategoryRequestData("token", "cat-1", "New Name", "Desc", null);
        
        when(mockTokenValidator.validate("token")).thenReturn(adminPrincipal);
        when(mockCategoryRepository.findById("cat-1")).thenReturn(existingCategory);
        when(mockCategoryRepository.findByName("New Name")).thenReturn(null);
        
        // Simulate DB Exception on Save
        doThrow(new RuntimeException("DB Connection Lost")).when(mockCategoryRepository).save(any(CategoryData.class));

        ArgumentCaptor<AddCategoryResponseData> captor = ArgumentCaptor.forClass(AddCategoryResponseData.class);
        useCase.execute(input);
        
        verify(mockOutputBoundary).present(captor.capture());
        AddCategoryResponseData output = captor.getValue();
        
        assertFalse(output.success);
        assertEquals("Lỗi hệ thống không xác định.", output.message);
    }
}
