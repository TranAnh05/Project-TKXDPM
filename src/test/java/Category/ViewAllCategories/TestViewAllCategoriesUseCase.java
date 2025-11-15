package Category.ViewAllCategories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Category.FakeCategoryRepository;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputBoundary;
import usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputData;
import usecase.ManageCategory.ViewAllCategories.ViewAllCategoryUsecase;

@ExtendWith(MockitoExtension.class)
public class TestViewAllCategoriesUseCase {
	// 1. "Giả lập" (Mock) các Dependencies
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private ViewAllCategoriesOutputBoundary categoryPresenter;
    
    // 2. "Tiêm" (Inject) các Mock vào Interactor
    @InjectMocks
    private ViewAllCategoryUsecase useCase; // (Tên class T3 của bạn)
    
    // trường hợp thành công
    @Test
    public void testExecute_SuccessCase_WithData() {
        // 1. Arrange (Dạy cho Mock Repository)
        List<CategoryData> mockDataList = List.of(
            new CategoryData(1, "Laptop"),
            new CategoryData(2, "Mouse")
        );
        // "Khi hàm findAll() được gọi, HÃY trả về list giả lập"
        when(categoryRepository.findAll()).thenReturn(mockDataList);

        // 2. Act (Hành động)
        useCase.execute();

        // 3. Assert (Khẳng định)
        // (Test Tầng 3 - Yêu cầu TDD 208)
        ViewAllCategoriesOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals(2, output.categories.size());
        assertEquals("Laptop", output.categories.get(0).name);
        
        // (Tùy chọn) Kiểm tra xem Presenter có được gọi đúng không
        verify(categoryPresenter, times(1)).present(output);
    }
    
    // trường hợp nhận danh sách rỗng
    @Test
    public void testExecute_SuccessCase_NoData() {
        // 1. Arrange (Dạy Mock trả về list rỗng)
        when(categoryRepository.findAll()).thenReturn(new ArrayList<CategoryData>());

        // 2. Act (Sửa lại)
        useCase.execute();
        
        // 3. Assert
        ViewAllCategoriesOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        assertEquals("Chưa có loại sản phẩm nào.", output.message);
        assertEquals(0, output.categories.size());
        
        verify(categoryPresenter, times(1)).present(output);
    }
    
    // trường hợp thất bại do lỗi kết nối với csdl
    @Test
    public void testExecute_Fail_DatabaseError() {
        // 1. Arrange (Dạy Mock ném lỗi hệ thống)
        when(categoryRepository.findAll()).thenThrow(new RuntimeException("CSDL sập"));

        // 2. Act
        useCase.execute();
        
        // 3. Assert
        ViewAllCategoriesOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Đã xảy ra lỗi hệ thống khi tải danh mục.", output.message);
    }
}
