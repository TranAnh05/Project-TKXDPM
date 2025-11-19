package cgx.com.usecase.ManageCategory.AddNewCategory;

public class AddCategoryResponseData {
	public boolean success;
    public String message;
    
    // Dữ liệu trả về khi thành công
    public String categoryId;
    public String name;
    public String parentCategoryId;
}
