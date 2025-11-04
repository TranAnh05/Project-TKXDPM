package application.ports.out.ManageCategory.ViewAllCategories;

import application.dtos.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputData;

public interface ViewAllCategoriesOutputBoundary {
	void present(ViewAllCategoriesOutputData output);
}
