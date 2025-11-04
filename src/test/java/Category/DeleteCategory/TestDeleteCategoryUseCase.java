package Category.DeleteCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Category.FakeCategoryRepository;
import adapters.ManageCategory.DeleteCategory.DeleteCategoryPresenter;
import adapters.ManageCategory.DeleteCategory.DeleteCategoryViewModel;
import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageCategory.DeleteCategory.DeleteCategoryInputData;
import application.ports.out.ManageCategory.CategoryRepository;
import application.usecases.ManageCategory.DeleteCategory.DeleteCategoryUsecase;

public class TestDeleteCategoryUseCase {
	private DeleteCategoryUsecase useCase;
	private CategoryRepository categoryRepo;
	private DeleteCategoryPresenter presenter;
    private DeleteCategoryViewModel viewModel;
    
    private CategoryData categoryToDelete;
    
    @BeforeEach
    public void setup() {
        // Phải cast (FakeCategoryRepository) để gọi hàm addFakeProduct
        categoryRepo = new FakeCategoryRepository(); 
        viewModel = new DeleteCategoryViewModel();
        presenter = new DeleteCategoryPresenter(viewModel);
        useCase = new DeleteCategoryUsecase(categoryRepo, presenter);
        
        // Dữ liệu mồi: Thêm 1 category (ID 1)
        categoryToDelete = categoryRepo.save(new CategoryData(0, "Laptop", "{}"));	
    }
    
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange
        DeleteCategoryInputData input = new DeleteCategoryInputData(categoryToDelete.id); // ID 1

        // 2. Act
        useCase.execute(input);

        // 3. Assert (Kiểm tra ViewModel)
        assertEquals(true, useCase.getOutputData().success);
        assertEquals("Đã xóa thành công loại sản phẩm: Laptop", useCase.getOutputData().message);
        
        // Kiểm tra CSDL giả
        assertNull(categoryRepo.findById(categoryToDelete.id));
    }
    
    @Test
    public void testExecute_Fail_NotFound() {
        // 1. Arrange: Xóa ID 99
        DeleteCategoryInputData input = new DeleteCategoryInputData(99);
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        assertEquals(false, useCase.getOutputData().success);
        assertEquals("Không tìm thấy loại sản phẩm để xóa.", useCase.getOutputData().message);
    }
    
    @Test
    public void testExecute_Fail_Business_CategoryInUse() {
        // 1. Arrange: Thêm 1 sản phẩm giả mạo thuộc Category ID 1
        ((FakeCategoryRepository)categoryRepo).addFakeProduct(101, categoryToDelete.id);
        
        DeleteCategoryInputData input = new DeleteCategoryInputData(categoryToDelete.id); // ID 1
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (UseCase bắt lỗi nghiệp vụ)
        assertEquals(false, useCase.getOutputData().success);
        assertEquals("Không thể xóa. Loại sản phẩm này đang chứa 1 sản phẩm.", useCase.getOutputData().message);
        
        // Kiểm tra CSDL giả (KHÔNG bị xóa)
        assertNotNull(categoryRepo.findById(categoryToDelete.id));
    }
}
