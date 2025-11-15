package Category.AddNewCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import Category.FakeCategoryRepository;
import adapters.ManageCategory.AddNewCategory.AddNewCategoryPresenter;
import adapters.ManageCategory.AddNewCategory.AddNewCategoryViewModel;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageCategory.AddNewCategory.AddNewCategoryInputBoundary;
import usecase.ManageCategory.AddNewCategory.AddNewCategoryInputData;
import usecase.ManageCategory.AddNewCategory.AddNewCategoryOutputBoundary;
import usecase.ManageCategory.AddNewCategory.AddNewCategoryOutputData;
import usecase.ManageCategory.AddNewCategory.AddNewCategoryUsecase;
import usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputData;

@ExtendWith(MockitoExtension.class)
public class TestAddNewCategoryUseCase {
	// 1. "Giả lập" (Mock) các Dependencies
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private AddNewCategoryOutputBoundary categoryPresenter; // (Presenter bị mock)
    
    // 2. "Tiêm" (Inject) các Mock vào Interactor
    @InjectMocks
    private AddNewCategoryUsecase useCase; // (Tên class T3 của bạn)
    
    // Trường hợp thành công
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange (Sắp xếp)
        AddNewCategoryInputData input = new AddNewCategoryInputData("Laptop");
        
        // "Dạy" (Stub) cho Mock Repository (T1):
        when(categoryRepository.findByName("Laptop")).thenReturn(null);
        
        // "Dạy" (Stub) cho Mock Repository (T1):
        CategoryData savedData = new CategoryData(1, "Laptop");
        when(categoryRepository.save(any(CategoryData.class))).thenReturn(savedData);

        // 2. Act (Hành động)
        useCase.execute(input);

        // 3. Assert (Khẳng định) (Test Tầng 3 - Yêu cầu TDD 208)
        // Kiểm tra xem OutputData (T3) có đúng không
        AddNewCategoryOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals(1, output.newCategory.id);
        
        // (Tùy chọn) Kiểm tra xem Presenter (T2) có được gọi đúng 1 lần không
        verify(categoryPresenter, times(1)).present(output);
    }
    
    // Trường hợp thất bại do tên trống
    @Test
    public void testExecute_Fail_Validation_EmptyName() {
        // 1. Arrange (Tên trống)
        AddNewCategoryInputData input = new AddNewCategoryInputData("");

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Kiểm tra OutputData T3)
        AddNewCategoryOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Tên loại sản phẩm không được để trống.", output.message);
        
        // (Quan trọng) Khẳng định rằng Tầng 1 (Database) KHÔNG bao giờ được gọi
        verify(categoryRepository, never()).save(any(CategoryData.class));
    }
    
    //  Trường hợp thất bại do tên trùng
    @Test
    public void testExecute_Fail_Business_DuplicateName() {
        // 1. Arrange
        AddNewCategoryInputData input = new AddNewCategoryInputData("Laptop");
        
        // Dạy: "Khi findByName("Laptop") được gọi, HÃY trả về 1 DTO (giả lập đã tồn tại)"
        when(categoryRepository.findByName("Laptop")).thenReturn(new CategoryData(1, "Laptop"));

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        AddNewCategoryOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Tên loại sản phẩm này đã tồn tại.", output.message);
        
        // (Quan trọng) Khẳng định rằng hàm 'save' KHÔNG bao giờ được gọi
        verify(categoryRepository, never()).save(any(CategoryData.class));
    }
}
