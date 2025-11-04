package adapters.ManageCategory.GetCategoryTemplate;

import application.dtos.ManageCategory.GetCategoryTemplate.GetCategoryTemplateOutputData;
import application.ports.out.ManageCategory.GetCategoryTemplate.GetCategoryTemplateOutputBoundary;

public class GetCategoryTemplatePresenter implements GetCategoryTemplateOutputBoundary{
	private GetCategoryTemplateViewModel viewModel;

    public GetCategoryTemplatePresenter(GetCategoryTemplateViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
    public GetCategoryTemplateViewModel getViewModel() {
        return this.viewModel;
    }
    
	@Override
	public void present(GetCategoryTemplateOutputData output) {
		viewModel.message = output.message;
		viewModel.success = String.valueOf(output.success);
		viewModel.attributeTemplate = output.attributeTemplate;
	}

}
