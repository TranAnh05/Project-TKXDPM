package Category.ViewAllCategories;

import org.junit.jupiter.api.BeforeEach;
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
public class ViewAllCategoriesUseCaseTest {

    @Mock private ICategoryRepository mockCategoryRepository;
    @Mock private ViewAllCategoriesOutputBoundary mockOutputBoundary;

    private ViewAllCategoriesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewAllCategoriesUseCase(mockCategoryRepository, mockOutputBoundary);
    }

    @Test
    void test_execute_success() {
        // ARRANGE
        List<CategoryData> mockList = Arrays.asList(
            new CategoryData("1", "C1", "D1", null, Instant.now(), Instant.now()),
            new CategoryData("2", "C2", "D2", "1", Instant.now(), Instant.now())
        );
        when(mockCategoryRepository.findAll()).thenReturn(mockList);

        ArgumentCaptor<ViewAllCategoriesResponseData> captor = ArgumentCaptor.forClass(ViewAllCategoriesResponseData.class);

        // ACT
        useCase.execute();

        // ASSERT
        verify(mockCategoryRepository).findAll();
        verify(mockOutputBoundary).present(captor.capture());
        
        ViewAllCategoriesResponseData output = captor.getValue();
        assertTrue(output.success);
        assertEquals(2, output.categories.size());
    }

    @Test
    void test_execute_failure_dbCrash() {
        // ARRANGE
        doThrow(new RuntimeException("DB Error")).when(mockCategoryRepository).findAll();
        ArgumentCaptor<ViewAllCategoriesResponseData> captor = ArgumentCaptor.forClass(ViewAllCategoriesResponseData.class);

        // ACT
        useCase.execute();

        // ASSERT
        verify(mockOutputBoundary).present(captor.capture());
        assertFalse(captor.getValue().success);
        assertEquals("Lỗi hệ thống không xác định.", captor.getValue().message);
    }
}