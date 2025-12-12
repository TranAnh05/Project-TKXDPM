package Category.ViewAllCategories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputBoundary;
import cgx.com.usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesResponseData;
import cgx.com.usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesUseCase;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewAllCategoriesUseCaseTest {

    @Mock
    private ICategoryRepository categoryRepository;
    
    @Mock
    private ViewAllCategoriesOutputBoundary outputBoundary;

    private ViewAllCategoriesUseCase viewAllCategoriesUseCase;

    @BeforeEach
    void setUp() {
        viewAllCategoriesUseCase = new ViewAllCategoriesUseCase(categoryRepository, outputBoundary);
    }
    
    @Test
    @DisplayName("Success: Lấy danh sách danh mục thành công có dữ liệu")
    void execute_ShouldSucceed_WhenListHasData() {
        CategoryData cat1 = new CategoryData("id1", "Laptop", "Desc 1", null, Instant.now(), Instant.now());
        CategoryData cat2 = new CategoryData("id2", "MacBook", "Apple", "id1", Instant.now(), Instant.now());
        List<CategoryData> mockData = Arrays.asList(cat1, cat2);

        when(categoryRepository.findAll()).thenReturn(mockData);

        // Act
        viewAllCategoriesUseCase.execute();

        // Assert
        ArgumentCaptor<ViewAllCategoriesResponseData> captor = ArgumentCaptor.forClass(ViewAllCategoriesResponseData.class);
        verify(outputBoundary).present(captor.capture());
        ViewAllCategoriesResponseData output = captor.getValue();

        assertTrue(output.success);
        assertEquals("Lấy danh sách danh mục thành công.", output.message);
        
        assertEquals(2, output.categories.size());
        assertEquals("Laptop", output.categories.get(0).name);
        assertEquals("MacBook", output.categories.get(1).name);
    }
}