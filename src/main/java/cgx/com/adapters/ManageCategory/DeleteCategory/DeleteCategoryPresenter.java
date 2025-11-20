package cgx.com.adapters.ManageCategory.DeleteCategory;

import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryOutputBoundary;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryResponseData;

public class DeleteCategoryPresenter implements DeleteCategoryOutputBoundary {

    private DeleteCategoryViewModel viewModel;

    public DeleteCategoryPresenter(DeleteCategoryViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(DeleteCategoryResponseData responseData) {
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;
        viewModel.deletedId = responseData.deletedCategoryId;
    }
    
    public DeleteCategoryViewModel getModel() {
        return this.viewModel;
    }
}