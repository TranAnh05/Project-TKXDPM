package application.ports.in.ManageCategory.GetCategoryTemplate;

import application.dtos.ManageCategory.GetCategoryTemplate.GetCategoryTemplateInputData;

public interface GetCategoryTemplateInputBoundary {
	void execute(GetCategoryTemplateInputData input);
}
