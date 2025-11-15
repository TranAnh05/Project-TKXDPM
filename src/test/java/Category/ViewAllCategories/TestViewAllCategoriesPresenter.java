package Category.ViewAllCategories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesPresenter;
import adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesViewModel;
import usecase.ManageCategory.CategoryOutputData;
import usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputData;

public class TestViewAllCategoriesPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        ViewAllCategoriesViewModel viewModel = new ViewAllCategoriesViewModel();
        ViewAllCategoriesPresenter presenter = new ViewAllCategoriesPresenter(viewModel);
        
        ViewAllCategoriesOutputData output = new ViewAllCategoriesOutputData();
        output.success = true; // <-- boolean
        
        CategoryOutputData cat1 = new CategoryOutputData();
        cat1.id = 5; // <-- int
        cat1.name = "Test";
        output.categories = List.of(cat1);

        // 2. Act
        presenter.present(output);

        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success); // boolean -> String
        assertEquals("5", viewModel.categories.get(0).id); // int -> String
        assertEquals("Test", viewModel.categories.get(0).name);
    }
}
