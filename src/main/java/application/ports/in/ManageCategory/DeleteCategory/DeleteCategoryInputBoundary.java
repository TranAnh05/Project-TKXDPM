package application.ports.in.ManageCategory.DeleteCategory;

import application.dtos.ManageCategory.DeleteCategory.DeleteCategoryInputData;

public interface DeleteCategoryInputBoundary {
	void execute(DeleteCategoryInputData input);
}
