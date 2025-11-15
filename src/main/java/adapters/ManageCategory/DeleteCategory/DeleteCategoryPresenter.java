package adapters.ManageCategory.DeleteCategory;

import usecase.ManageCategory.DeleteCategory.DeleteCategoryOutputBoundary;
import usecase.ManageCategory.DeleteCategory.DeleteCategoryOutputData;

public class DeleteCategoryPresenter implements DeleteCategoryOutputBoundary{
	private DeleteCategoryViewModel viewModel;

    public DeleteCategoryPresenter(DeleteCategoryViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
    public DeleteCategoryViewModel getViewModel() {
        return this.viewModel;
    }
    
	@Override
	public void present(DeleteCategoryOutputData output) {
		viewModel.message = output.message;
		viewModel.success = String.valueOf(output.success);
	}

}
