package application.ports.out.ManageCategory.UpdateCategory;

import application.dtos.ManageCategory.UpdateCategory.UpdateCategoryOutputData;

public interface UpdateCategoryOutputBoundary {
	void present(UpdateCategoryOutputData output);
}
