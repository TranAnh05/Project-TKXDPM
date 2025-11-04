package application.ports.out.ManageCategory.DeleteCategory;

import application.dtos.ManageCategory.DeleteCategory.DeleteCategoryOutputData;

public interface DeleteCategoryOutputBoundary {
	void present(DeleteCategoryOutputData output);
}
