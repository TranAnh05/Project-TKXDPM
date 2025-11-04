package adapters.ManageCategory.UpdateCategory;

import adapters.ManageCategory.AddNewCategory.CategoryViewDTO;
import application.dtos.ManageCategory.CategoryOutputData;
import application.dtos.ManageCategory.UpdateCategory.UpdateCategoryOutputData;
import application.ports.out.ManageCategory.UpdateCategory.UpdateCategoryOutputBoundary;

public class UpdateCategoryPresenter implements UpdateCategoryOutputBoundary{
	private UpdateCategoryViewModel viewModel;

    public UpdateCategoryPresenter(UpdateCategoryViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
    public UpdateCategoryViewModel getModel() {
    	return viewModel;
    }

	@Override
	public void present(UpdateCategoryOutputData output) {
		CategoryViewDTO viewDTO = null;
        if (output.updatedCategory != null) {
            viewDTO = mapToViewDTO(output.updatedCategory);
        }
        
        viewModel.message = output.message;
        viewModel.success = String.valueOf(output.success);
        viewModel.updatedCategory = viewDTO;
	}

	private CategoryViewDTO mapToViewDTO(CategoryOutputData data) {
		CategoryViewDTO dto = new CategoryViewDTO();
	    dto.id = String.valueOf(data.id); 
	    dto.name = data.name;
	    dto.attributeTemplate = data.attributeTemplate;
	    return dto;
	}

}
