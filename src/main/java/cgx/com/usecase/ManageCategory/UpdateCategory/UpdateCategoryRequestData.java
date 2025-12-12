package cgx.com.usecase.ManageCategory.UpdateCategory;

public class UpdateCategoryRequestData {
	public String authToken;
    public String categoryId; // ID của danh mục cần sửa
    public String name;
    public String description;
    public String parentCategoryId; // ID danh mục cha mới (có thể null)
    
    public UpdateCategoryRequestData(String authToken, String categoryId, String name, String description, String parentCategoryId) {
        this.authToken = authToken;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
    }

	public UpdateCategoryRequestData() {
		// TODO Auto-generated constructor stub
	}
}
