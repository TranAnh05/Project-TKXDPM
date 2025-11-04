package application.ports.out.ManageCategory.GetCategoryTemplate;

import application.dtos.ManageCategory.GetCategoryTemplate.GetCategoryTemplateOutputData;

public interface GetCategoryTemplateOutputBoundary {
	void present(GetCategoryTemplateOutputData output);
}
