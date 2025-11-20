package Category.DeleteCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageCategory.DeleteCategory.DeleteCategoryPresenter;
import cgx.com.adapters.ManageCategory.DeleteCategory.DeleteCategoryViewModel;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryResponseData;

import static org.junit.jupiter.api.Assertions.*;

public class DeleteCategoryPresenterTest {
    
    private DeleteCategoryPresenter presenter;
    private DeleteCategoryViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new DeleteCategoryViewModel();
        presenter = new DeleteCategoryPresenter(viewModel);
    }
    
    @Test
    void test_present_success() {
        DeleteCategoryResponseData response = new DeleteCategoryResponseData();
        response.success = true;
        response.message = "Delete Success";
        response.deletedCategoryId = "cat-123";
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertEquals("Delete Success", viewModel.message);
        assertEquals("cat-123", viewModel.deletedId);
    }
    
    @Test
    void test_present_failure() {
        DeleteCategoryResponseData response = new DeleteCategoryResponseData();
        response.success = false;
        response.message = "Error: Has children";
        // deletedCategoryId remains null
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertEquals("Error: Has children", viewModel.message);
        assertNull(viewModel.deletedId);
    }
}