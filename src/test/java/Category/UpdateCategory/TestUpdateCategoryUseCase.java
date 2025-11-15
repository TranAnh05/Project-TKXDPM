package Category.UpdateCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Category.FakeCategoryRepository;
import adapters.ManageCategory.UpdateCategory.UpdateCategoryPresenter;
import adapters.ManageCategory.UpdateCategory.UpdateCategoryViewModel;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageCategory.UpdateCategory.UpdateCategoryInputData;
import usecase.ManageCategory.UpdateCategory.UpdateCategoryOutputBoundary;
import usecase.ManageCategory.UpdateCategory.UpdateCategoryOutputData;
import usecase.ManageCategory.UpdateCategory.UpdateCategoryUsecase;

@ExtendWith(MockitoExtension.class)
public class TestUpdateCategoryUseCase {
	// 1. "Giả lập" (Mock) các Dependencies
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private UpdateCategoryOutputBoundary categoryPresenter;
    
    // 2. "Tiêm" (Inject) các Mock vào Interactor
    @InjectMocks
    private UpdateCategoryUsecase useCase; // (Tên class T3 của bạn)
    
    // trường hợp thành công
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange (Dạy Mock)
        UpdateCategoryInputData input = new UpdateCategoryInputData(1, "Laptop Gaming");
        
        // Dạy: "Khi findByName("Laptop Gaming") được gọi, HÃY trả về null"
        when(categoryRepository.findByName("Laptop Gaming")).thenReturn(null);
        
        // Dạy: "Khi findById(1) được gọi, HÃY trả về DTO này"
        CategoryData existingData = new CategoryData(1, "Laptop");
        when(categoryRepository.findById(1)).thenReturn(existingData);
        
        // Dạy: "Khi update(...) được gọi, HÃY trả về DTO này"
        CategoryData updatedData = new CategoryData(1, "Laptop Gaming");
        when(categoryRepository.update(any(CategoryData.class))).thenReturn(updatedData);

        // 2. Act
        useCase.execute(input);

        // 3. Assert (Kiểm tra OutputData)
        UpdateCategoryOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals(1, output.updatedCategory.id);
        assertEquals("Laptop Gaming", output.updatedCategory.name);
    }
    
    // trường hợp thành công: Người dùng nhấn save nhưng không đổi tên
    @Test
    public void testExecute_Success_Rename_ToExistingName_But_SameId() {
        // (Trường hợp đặc biệt: Người dùng nhấn "Save" mà không đổi tên)
        // 1. Arrange
        UpdateCategoryInputData input = new UpdateCategoryInputData(1, "Laptop"); // Sửa ID 1 thành "Laptop"
        
        // Dạy: "Tên 'Laptop' đã tồn tại, và nó thuộc ID 1"
        when(categoryRepository.findByName("Laptop")).thenReturn(new CategoryData(1, "Laptop"));
        
        // (Tất cả các mock khác giống hệt SuccessCase)
        when(categoryRepository.findById(1)).thenReturn(new CategoryData(1, "Laptop"));
        when(categoryRepository.update(any(CategoryData.class))).thenReturn(new CategoryData(1, "Laptop"));

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (PHẢI THÀNH CÔNG)
        UpdateCategoryOutputData output = useCase.getOutputData();
        assertTrue(output.success);
    }
    
    // trường hợp thất bại do tên trống
    @Test
    public void testExecute_Fail_Validation_EmptyName() {
        // 1. Arrange (Tên trống)
        UpdateCategoryInputData input = new UpdateCategoryInputData(1, "");

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        UpdateCategoryOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Tên loại sản phẩm không được để trống.", output.message);
        
        // Khẳng định: Không có tương tác CSDL nào xảy ra
        verify(categoryRepository, never()).findById(anyInt());
        verify(categoryRepository, never()).update(any(CategoryData.class));
    }
    
    // trường hợp thất bại do duplicate name
    @Test
    public void testExecute_Fail_Business_DuplicateName() {
        // 1. Arrange
        UpdateCategoryInputData input = new UpdateCategoryInputData(1, "Mouse"); // Sửa ID 1 thành "Mouse"
        
        // Dạy: "Tên 'Mouse' đã tồn tại, và nó thuộc ID 2"
        when(categoryRepository.findByName("Mouse")).thenReturn(new CategoryData(2, "Mouse"));

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        UpdateCategoryOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Tên loại sản phẩm này đã tồn tại.", output.message);
    }
    
    @Test
    public void testExecute_Fail_NotFound() {
        // 1. Arrange
        UpdateCategoryInputData input = new UpdateCategoryInputData(99, "Hacker");
        
        // Dạy: "findByName("Hacker") trả về null (hợp lệ)"
        when(categoryRepository.findByName("Hacker")).thenReturn(null);
        
        // Dạy: "findById(99) trả về null (không tìm thấy)"
        when(categoryRepository.findById(99)).thenReturn(null);

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        UpdateCategoryOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Không tìm thấy loại sản phẩm để cập nhật.", output.message);
    }
    
}
