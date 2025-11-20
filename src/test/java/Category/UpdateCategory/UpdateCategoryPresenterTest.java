package Category.UpdateCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageCategory.UpdateCategory.UpdateCategoryPresenter;
import cgx.com.adapters.ManageCategory.UpdateCategory.UpdateCategoryViewModel;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryResponseData;

public class UpdateCategoryPresenterTest {
	private UpdateCategoryPresenter presenter;
    private UpdateCategoryViewModel viewModel;
    private AddCategoryResponseData responseData;
    
    @BeforeEach
    void setUp() {
        viewModel = new UpdateCategoryViewModel();
        presenter = new UpdateCategoryPresenter(viewModel);
        responseData = new AddCategoryResponseData();
    }
    
    /**
     * Test Success: Category has a parent
     */
    @Test
    void test_present_success_withParent() {
        responseData.success = true;
        responseData.message = "Update OK";
        responseData.categoryId = "cat-1";
        responseData.name = "Laptop";
        responseData.parentCategoryId = "cat-parent";
        
        presenter.present(responseData);
        
        assertEquals("true", viewModel.success);
        assertEquals("Update OK", viewModel.message);
        assertNotNull(viewModel.updatedCategory);
        assertEquals("cat-1", viewModel.updatedCategory.id);
        assertEquals("Laptop", viewModel.updatedCategory.name);
        assertEquals("cat-parent", viewModel.updatedCategory.parentId);
    }

    /**
     * Test Success: Category is Root (No parent)
     * Ensures null parentId in DTO is converted to "null" string in ViewModel
     */
    @Test
    void test_present_success_rootCategory() {
        responseData.success = true;
        responseData.message = "Update OK";
        responseData.categoryId = "cat-1";
        responseData.name = "Root Cat";
        responseData.parentCategoryId = null;
        
        presenter.present(responseData);
        
        assertEquals("true", viewModel.success);
        assertNotNull(viewModel.updatedCategory);
        assertEquals("null", viewModel.updatedCategory.parentId);
    }
    
    /**
     * Test Failure
     */
    @Test
    void test_present_failure() {
        responseData.success = false;
        responseData.message = "Error msg";
        
        presenter.present(responseData);
        
        assertEquals("false", viewModel.success);
        assertEquals("Error msg", viewModel.message);
        assertNull(viewModel.updatedCategory);
    }
}
