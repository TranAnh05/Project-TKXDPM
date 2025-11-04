package application.ports.out.ManageCategory.AddNewCategory;

import application.dtos.ManageCategory.AddNewCategory.AddNewCategoryOutputData;

public interface AddNewCategoryOutputBoundary {
	void present(AddNewCategoryOutputData output);
}
