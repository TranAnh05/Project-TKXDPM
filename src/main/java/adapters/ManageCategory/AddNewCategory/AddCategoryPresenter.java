package adapters.ManageCategory.AddNewCategory;

import adapters.ManageCategory.CategoryViewDTO;
import usecase.ManageCategory.AddNewCategory.AddCategoryOutputBoundary;
import usecase.ManageCategory.AddNewCategory.AddCategoryResponseData;

public class AddCategoryPresenter implements AddCategoryOutputBoundary{
    private AddCategoryViewModel viewModel;
	
    public AddCategoryPresenter(AddCategoryViewModel viewModel) {
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
        viewModel.category = viewDTO;
	}

	public AddCategoryViewModel getModel() {
        return this.viewModel;
    }
}
