package Category.AddNewCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Category.FakeCategoryRepository;
import adapters.ManageCategory.AddNewCategory.AddNewCategoryPresenter;
import adapters.ManageCategory.AddNewCategory.AddNewCategoryViewModel;
import application.dtos.ManageCategory.AddNewCategory.AddNewCategoryInputData;
import application.ports.in.ManageCategory.AddNewCategory.AddNewCategoryInputBoundary;
import application.ports.out.ManageCategory.CategoryRepository;
import application.usecases.ManageCategory.AddNewCategory.AddNewCategoryUsecase;

public class TestAddNewCategoryUseCase {
	private AddNewCategoryUsecase useCase;
	private CategoryRepository categoryRepo;
	private AddNewCategoryPresenter presenter;
    private AddNewCategoryViewModel viewModel;
    
    @BeforeEach
    public void setup() {
        categoryRepo = new FakeCategoryRepository();
        viewModel = new AddNewCategoryViewModel();
        presenter = new AddNewCategoryPresenter(viewModel);
        useCase = new AddNewCategoryUsecase(categoryRepo, presenter);
    }
    
    @Test
    public void testExecute_SuccessCase() {
        AddNewCategoryInputData input = new AddNewCategoryInputData("Laptop", "{\"cpu\":\"text\"}");
        useCase.execute(input);
        assertEquals(true, useCase.getOutputData().success);
        assertNotNull(useCase.getOutputData().message);
        assertEquals(1, useCase.getOutputData().newCategory.id);
    }
}
