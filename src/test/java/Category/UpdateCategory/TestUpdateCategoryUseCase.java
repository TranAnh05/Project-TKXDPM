package Category.UpdateCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Category.FakeCategoryRepository;
import adapters.ManageCategory.UpdateCategory.UpdateCategoryPresenter;
import adapters.ManageCategory.UpdateCategory.UpdateCategoryViewModel;
import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageCategory.UpdateCategory.UpdateCategoryInputData;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageCategory.UpdateCategory.UpdateCategoryOutputBoundary;
import application.usecases.ManageCategory.UpdateCategory.UpdateCategoryUsecase;

public class TestUpdateCategoryUseCase {
	private UpdateCategoryUsecase useCase;
	private CategoryRepository categoryRepo;
	private UpdateCategoryPresenter presenter;
    private UpdateCategoryViewModel viewModel;
    
    private CategoryData existingLaptop;
    private CategoryData existingMouse;
    
    @BeforeEach
    public void setup() {
        categoryRepo = new FakeCategoryRepository();
        viewModel = new UpdateCategoryViewModel();
        presenter = new UpdateCategoryPresenter(viewModel);
        useCase = new UpdateCategoryUsecase(categoryRepo, presenter);
        
        // Thêm dữ liệu mồi vào CSDL giả
        existingLaptop = categoryRepo.save(new CategoryData(0, "Laptop", "{}")); // ID: 1
        existingMouse = categoryRepo.save(new CategoryData(0, "Mouse", "{}")); // ID: 2
    }
    
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange: Sửa "Laptop" (ID 1)
        UpdateCategoryInputData input = new UpdateCategoryInputData(
            existingLaptop.id, // 1
            "Laptop Gaming", 
            "{'cpu':'text'}"
        );

        // 2. Act
        useCase.execute(input);
        
        assertEquals(true, useCase.getOutputData().success);
        assertNotNull(useCase.getOutputData().message);
        assertEquals(1, useCase.getOutputData().updatedCategory.id);
        assertEquals("Laptop Gaming", useCase.getOutputData().updatedCategory.name);
        
        // Kiểm tra CSDL giả
        assertEquals("Laptop Gaming", categoryRepo.findById(1).name);
    }
    
    @Test
    public void testExecute_Fail_Validation_EmptyName() {
        // 1. Arrange: Sửa "Laptop" (ID 1) thành tên rỗng
        UpdateCategoryInputData input = new UpdateCategoryInputData(
            existingLaptop.id, "", "{}" // Tên trống
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (UseCase bắt lỗi từ Tầng 4)
        assertEquals(false, useCase.getOutputData().success);
        assertEquals("Tên loại sản phẩm không được để trống.", useCase.getOutputData().message);
    }
    
    @Test
    public void testExecute_Fail_Business_DuplicateName() {
        // 1. Arrange: Sửa "Laptop" (ID 1) thành "Mouse" (ID 2)
        UpdateCategoryInputData input = new UpdateCategoryInputData(
            existingLaptop.id, // 1
            "Mouse", // Tên bị trùng với ID 2
            "{}"
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (UseCase bắt lỗi nghiệp vụ)
        assertEquals(false, useCase.getOutputData().success);
        assertEquals("Tên loại sản phẩm này đã tồn tại.", useCase.getOutputData().message);
    }
    
    @Test
    public void testExecute_Fail_NotFound() {
        // 1. Arrange: Sửa ID 99 (không tồn tại)
        UpdateCategoryInputData input = new UpdateCategoryInputData(99, "Không tồn tại", "{}");
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        assertEquals(false, useCase.getOutputData().success);
        assertEquals("Không tìm thấy loại sản phẩm để cập nhật.", useCase.getOutputData().message);
    }
}
