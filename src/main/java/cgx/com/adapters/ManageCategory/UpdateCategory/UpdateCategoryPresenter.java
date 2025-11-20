package cgx.com.adapters.ManageCategory.UpdateCategory;

import cgx.com.adapters.ManageCategory.CategoryViewDTO;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryResponseData;
import cgx.com.usecase.ManageCategory.UpdateCategory.UpdateCategoryOutputBoundary;

public class UpdateCategoryPresenter implements UpdateCategoryOutputBoundary {

    private UpdateCategoryViewModel viewModel;

    public UpdateCategoryPresenter(UpdateCategoryViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(AddCategoryResponseData responseData) {
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        CategoryViewDTO viewDTO = null;
        if (responseData.success) {
            viewDTO = new CategoryViewDTO();
            viewDTO.id = responseData.categoryId;
            viewDTO.name = responseData.name;
            viewDTO.parentId = (responseData.parentCategoryId == null) ? "null" : responseData.parentCategoryId;
        }
        viewModel.updatedCategory = viewDTO;
    }
    
    public UpdateCategoryViewModel getModel() {
        return this.viewModel;
    }
}
