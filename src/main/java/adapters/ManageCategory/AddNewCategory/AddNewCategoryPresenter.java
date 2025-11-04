package adapters.ManageCategory.AddNewCategory;

import application.dtos.ManageCategory.CategoryOutputData;
import application.dtos.ManageCategory.AddNewCategory.AddNewCategoryOutputData;
import application.ports.out.ManageCategory.AddNewCategory.AddNewCategoryOutputBoundary;

public class AddNewCategoryPresenter implements AddNewCategoryOutputBoundary{
	private AddNewCategoryViewModel viewModel;
	
	public AddNewCategoryPresenter(AddNewCategoryViewModel viewModel) {
		this.viewModel = viewModel;
	}
	
	public AddNewCategoryViewModel getModel() {
		return viewModel;
	}


	@Override
	public void present(AddNewCategoryOutputData output) {
		CategoryViewDTO viewDTO = null;
		if (output.newCategory != null) {
            viewDTO = mapToViewDTO(output.newCategory);
        }
		viewModel.message = output.message;
		viewModel.success = String.valueOf(output.success);
		viewModel.newCategory = viewDTO;
		
	}

	private CategoryViewDTO mapToViewDTO(CategoryOutputData data) {
		CategoryViewDTO dto = new CategoryViewDTO();
        dto.id = String.valueOf(data.id);
        dto.name = data.name;
        dto.attributeTemplate = data.attributeTemplate;
        return dto;
	}

}
