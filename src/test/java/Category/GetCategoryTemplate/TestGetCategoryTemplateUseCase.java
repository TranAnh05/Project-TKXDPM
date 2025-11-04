package Category.GetCategoryTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Category.FakeCategoryRepository;
import adapters.ManageCategory.GetCategoryTemplate.GetCategoryTemplatePresenter;
import adapters.ManageCategory.GetCategoryTemplate.GetCategoryTemplateViewModel;
import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageCategory.GetCategoryTemplate.GetCategoryTemplateInputData;
import application.ports.out.ManageCategory.CategoryRepository;
import application.usecases.ManageCategory.GetCategoryTemplate.GetCategoryTemplateUsecase;

public class TestGetCategoryTemplateUseCase {
	private GetCategoryTemplateUsecase useCase;
	private CategoryRepository categoryRepo;
	private GetCategoryTemplatePresenter presenter;
    private GetCategoryTemplateViewModel viewModel;
    
    private CategoryData existingLaptop;
    
    @BeforeEach
    public void setup() {
        categoryRepo = new FakeCategoryRepository(); 
        viewModel = new GetCategoryTemplateViewModel();
        presenter = new GetCategoryTemplatePresenter(viewModel);
        useCase = new GetCategoryTemplateUsecase(categoryRepo, presenter);
        
        // Dữ liệu mồi: Thêm 1 category (ID 1)
        existingLaptop = categoryRepo.save(new CategoryData(0, "Laptop", "{\"cpu\":\"text\"}"));
    }
    
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange
        GetCategoryTemplateInputData input = new GetCategoryTemplateInputData(existingLaptop.id); // ID 1

        // 2. Act
        useCase.execute(input);
        
        assertTrue(useCase.getOutputData().success);
        assertEquals("{\"cpu\":\"text\"}", useCase.getOutputData().attributeTemplate);
    }

    @Test
    public void testExecute_Fail_NotFound() {
        // 1. Arrange: Lấy ID 99
        GetCategoryTemplateInputData input = new GetCategoryTemplateInputData(99);
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Kiểm tra ViewModel)
        assertEquals(false, useCase.getOutputData().success);
        assertEquals("Không tìm thấy loại sản phẩm.", useCase.getOutputData().message);
    }
}
