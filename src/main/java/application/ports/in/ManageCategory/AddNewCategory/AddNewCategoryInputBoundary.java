package application.ports.in.ManageCategory.AddNewCategory;

import application.dtos.ManageCategory.AddNewCategory.AddNewCategoryInputData;

public interface AddNewCategoryInputBoundary {
	void execute(AddNewCategoryInputData input);
}
