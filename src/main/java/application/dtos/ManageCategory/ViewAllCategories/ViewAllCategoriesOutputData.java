package application.dtos.ManageCategory.ViewAllCategories;

import java.util.List;

import application.dtos.ManageCategory.CategoryOutputData;

public class ViewAllCategoriesOutputData {
	public boolean success;
    public String message;
    public List<CategoryOutputData> categories;
}
