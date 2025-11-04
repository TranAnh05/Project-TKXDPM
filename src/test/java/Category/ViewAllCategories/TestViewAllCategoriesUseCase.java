package Category.ViewAllCategories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Category.FakeCategoryRepository;
import adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesPresenter;
import adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesViewModel;
import application.dtos.ManageCategory.CategoryData;
import application.ports.out.ManageCategory.CategoryRepository;
import application.usecases.ManageCategory.ViewAllCategories.ViewAllCategoryUsecase;

public class TestViewAllCategoriesUseCase {
	private ViewAllCategoryUsecase useCase;
	private CategoryRepository categoryRepo;
	private ViewAllCategoriesPresenter presenter;
    private ViewAllCategoriesViewModel viewModel;
    
    @BeforeEach
    public void setup() {
        categoryRepo = new FakeCategoryRepository();
        viewModel = new ViewAllCategoriesViewModel();
        presenter = new ViewAllCategoriesPresenter(viewModel);
        useCase = new ViewAllCategoryUsecase(categoryRepo, presenter);
    }
    
    @Test
    public void testExecute_SuccessCase_WithData() {
        // 1. Arrange (Thêm 2 mục vào CSDL giả)
        categoryRepo.save(new CategoryData(0, "Laptop", "{}"));
        categoryRepo.save(new CategoryData(0, "Mouse", "{}"));

        // 2. Act
        useCase.execute();
        
        assertEquals(true, useCase.getOutputData().success);
        assertNull(useCase.getOutputData().message);
        assertEquals(2, useCase.getOutputData().categories.size());
        assertEquals("Laptop", useCase.getOutputData().categories.get(0).name);
    }
    
    @Test
    public void testExecute_SuccessCase_NoData() {
        // 1. Arrange (Không thêm gì)

        // 2. Act
        useCase.execute();

        // 3. Assert
        assertEquals(true, useCase.getOutputData().success);
        assertEquals("Chưa có loại sản phẩm nào.", useCase.getOutputData().message);
    }
}
