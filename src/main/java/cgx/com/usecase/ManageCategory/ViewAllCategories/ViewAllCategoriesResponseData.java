package cgx.com.usecase.ManageCategory.ViewAllCategories;

import java.util.List;

import cgx.com.usecase.ManageCategory.CategoryData;

public class ViewAllCategoriesResponseData {
	public boolean success;
    public String message;
    
    // Dữ liệu trả về: Danh sách phẳng tất cả danh mục
    // Presenter sẽ lo việc chuyển đổi thành cây (Tree)
    public List<CategoryData> categories;
}
