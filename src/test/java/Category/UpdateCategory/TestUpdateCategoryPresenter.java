package Category.UpdateCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import adapters.ManageCategory.UpdateCategory.UpdateCategoryPresenter;
import adapters.ManageCategory.UpdateCategory.UpdateCategoryViewModel;
import application.dtos.ManageCategory.CategoryOutputData;
import application.dtos.ManageCategory.UpdateCategory.UpdateCategoryOutputData;

public class TestUpdateCategoryPresenter {
	@Test
    public void testPresent_SuccessCase_Conversion() {
        // 1. Arrange
        UpdateCategoryViewModel viewModel = new UpdateCategoryViewModel();
        UpdateCategoryPresenter presenter = new UpdateCategoryPresenter(viewModel);
        
        // Dữ liệu từ Tầng 3 (UseCase)
        UpdateCategoryOutputData output = new UpdateCategoryOutputData();
        output.success = true;
        output.updatedCategory = new CategoryOutputData();
        output.updatedCategory.id = 5; // <-- int
        output.updatedCategory.name = "Test";

        // 2. Act
        presenter.present(output);

        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success); // boolean -> String
        assertEquals("5", viewModel.updatedCategory.id); // int -> String
        assertEquals("Test", viewModel.updatedCategory.name);
    }
	
	@Test
    public void testPresent_FailCase_Conversion() {
        // 1. Arrange
        UpdateCategoryViewModel viewModel = new UpdateCategoryViewModel();
        UpdateCategoryPresenter presenter = new UpdateCategoryPresenter(viewModel);
        
        // Dữ liệu từ Tầng 3 (UseCase)
        UpdateCategoryOutputData output = new UpdateCategoryOutputData();
        output.success = false; // <-- boolean
        output.message = "Lỗi";
        output.updatedCategory = null; // Không có data

        // 2. Act
        presenter.present(output);

        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("false", viewModel.success); // boolean -> String
        assertEquals("Lỗi", viewModel.message);
        assertNull(viewModel.updatedCategory);
    }
}
