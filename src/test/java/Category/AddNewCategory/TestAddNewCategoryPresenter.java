package Category.AddNewCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.ManageCategory.AddNewCategory.AddNewCategoryPresenter;
import adapters.ManageCategory.AddNewCategory.AddNewCategoryViewModel;
import usecase.ManageCategory.CategoryOutputData;
import usecase.ManageCategory.AddNewCategory.AddNewCategoryOutputData;

public class TestAddNewCategoryPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        AddNewCategoryViewModel viewModel = new AddNewCategoryViewModel();
        AddNewCategoryPresenter presenter = new AddNewCategoryPresenter(viewModel);
        AddNewCategoryOutputData output = new AddNewCategoryOutputData();
        output.success = true; // <-- boolean
        output.newCategory = new CategoryOutputData();
        output.newCategory.id = 5; // <-- int
        // 2. Act
        presenter.present(output);
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success); // boolean -> String
        assertEquals("5", viewModel.newCategory.id); // int -> String
    }
}
