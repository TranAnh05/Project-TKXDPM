package Category.DeleteCategory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.ManageCategory.DeleteCategory.DeleteCategoryPresenter;
import adapters.ManageCategory.DeleteCategory.DeleteCategoryViewModel;
import usecase.ManageCategory.DeleteCategory.DeleteCategoryOutputData;

public class TestDeleteCategoryPresenter {
	@Test
    public void testPresent_SuccessCase() {
        // 1. Arrange
        DeleteCategoryViewModel viewModel = new DeleteCategoryViewModel();
        DeleteCategoryPresenter presenter = new DeleteCategoryPresenter(viewModel);
        
        DeleteCategoryOutputData output = new DeleteCategoryOutputData();
        output.success = true;
        output.message = "Xóa thành công";

        // 2. Act
        presenter.present(output);

        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success);
        assertEquals("Xóa thành công", viewModel.message);
    }
	
	@Test
    public void testPresent_FailCase() {
        DeleteCategoryViewModel viewModel = new DeleteCategoryViewModel();
        DeleteCategoryPresenter presenter = new DeleteCategoryPresenter(viewModel);
        DeleteCategoryOutputData output = new DeleteCategoryOutputData();
        output.success = false;
        output.message = "Lỗi";
        
        presenter.present(output);
        
        assertEquals("false", viewModel.success);
        assertEquals("Lỗi", viewModel.message);
    }
}
