package adapters.ManageCategory.ViewAllCategories;

import java.util.ArrayList;
import java.util.List;

import adapters.ManageCategory.AddNewCategory.CategoryViewDTO;
import application.dtos.ManageCategory.CategoryOutputData;
import application.dtos.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputData;
import application.ports.out.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputBoundary;

public class ViewAllCategoriesPresenter implements ViewAllCategoriesOutputBoundary{
	private ViewAllCategoriesViewModel viewModel;
	
	public ViewAllCategoriesPresenter() {
		
	}

    public ViewAllCategoriesPresenter(ViewAllCategoriesViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
    public ViewAllCategoriesViewModel getModel() {
    	return viewModel;
    }
    
	@Override
	public void present(ViewAllCategoriesOutputData output) {
		List<CategoryViewDTO> viewDTOs = new ArrayList<>();
		if (output.categories != null) {
            for (CategoryOutputData categoryData : output.categories) {
                viewDTOs.add(mapToViewDTO(categoryData));
            }
        }
		
		viewModel.message = output.message;
		viewModel.success = String.valueOf(output.success);
		viewModel.categories = viewDTOs;
	}

	private CategoryViewDTO mapToViewDTO(CategoryOutputData data) {
		CategoryViewDTO dto = new CategoryViewDTO();
        dto.id = String.valueOf(data.id); 
        dto.name = data.name;
        dto.attributeTemplate = data.attributeTemplate;
        return dto;
	}

}
