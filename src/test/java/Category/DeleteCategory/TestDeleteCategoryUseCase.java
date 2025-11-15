package Category.DeleteCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
//Import Mockito
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import adapters.ManageCategory.DeleteCategory.DeleteCategoryPresenter;
import adapters.ManageCategory.DeleteCategory.DeleteCategoryViewModel;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageCategory.DeleteCategory.DeleteCategoryInputData;
import usecase.ManageCategory.DeleteCategory.DeleteCategoryOutputBoundary;
import usecase.ManageCategory.DeleteCategory.DeleteCategoryOutputData;
import usecase.ManageCategory.DeleteCategory.DeleteCategoryUsecase;

@ExtendWith(MockitoExtension.class)
public class TestDeleteCategoryUsecase {
	@Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private DeleteCategoryOutputBoundary categoryPresenter;
    
    @InjectMocks
    private DeleteCategoryUsecase useCase;
    
    // trường hợp xóa thành công
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange (Dạy Mock)
        DeleteCategoryInputData input = new DeleteCategoryInputData(1);
        
        // Dạy: "Khi findById(1) được gọi, HÃY trả về 1 CategoryData"
        CategoryData existingData = new CategoryData(1, "Laptop");
        when(categoryRepository.findById(1)).thenReturn(existingData);
        
        // Dạy: "Khi countProductsByCategoryId(1) được gọi, HÃY trả về 0 (không có SP)"
        when(categoryRepository.countProductsByCategoryId(1)).thenReturn(0);
        
        // 2. Act
        useCase.execute(input);

        // 3. Assert (Kiểm tra OutputData T3)
        DeleteCategoryOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals("Đã xóa thành công loại sản phẩm: Laptop", output.message);
        
        // (Quan trọng) Kiểm tra xem CSDL (Mock) có được gọi 'delete' đúng 1 lần không
        verify(categoryRepository, times(1)).deleteById(1);
    }
    
    // trường hợp xóa thất bại 
    @Test
    public void testExecute_Fail_NotFound() {
        // 1. Arrange
        DeleteCategoryInputData input = new DeleteCategoryInputData(99);
        
        // Dạy: "Khi findById(99) được gọi, HÃY trả về null"
        when(categoryRepository.findById(99)).thenReturn(null);
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Kiểm tra OutputData T3)
        DeleteCategoryOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Không tìm thấy loại sản phẩm để xóa.", output.message);
        
        // Khẳng định: 'delete' KHÔNG bao giờ được gọi
        verify(categoryRepository, never()).deleteById(anyInt());
    }
    
    // trường hợp thất bại do đang có trong trong một sản phẩm nào đó
    @Test
    public void testExecute_Fail_Business_CategoryInUse() {
        // 1. Arrange
        DeleteCategoryInputData input = new DeleteCategoryInputData(1);
        
        // Dạy: "Khi findById(1) được gọi, HÃY trả về 1 CategoryData"
        when(categoryRepository.findById(1)).thenReturn(new CategoryData(1, "Laptop"));
        
        // Dạy: "Khi countProductsByCategoryId(1) được gọi, HÃY trả về 5 (còn 5 SP)"
        when(categoryRepository.countProductsByCategoryId(1)).thenReturn(5);

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        DeleteCategoryOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Không thể xóa. Loại sản phẩm này đang chứa 5 sản phẩm.", output.message);
        
        // Khẳng định: 'delete' KHÔNG bao giờ được gọi
        verify(categoryRepository, never()).deleteById(anyInt());
    }
}
