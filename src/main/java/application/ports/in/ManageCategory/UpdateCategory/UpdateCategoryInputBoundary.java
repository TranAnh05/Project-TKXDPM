package application.ports.in.ManageCategory.UpdateCategory;

import application.dtos.ManageCategory.UpdateCategory.UpdateCategoryInputData;

public interface UpdateCategoryInputBoundary {
	void execute(UpdateCategoryInputData input);
}
