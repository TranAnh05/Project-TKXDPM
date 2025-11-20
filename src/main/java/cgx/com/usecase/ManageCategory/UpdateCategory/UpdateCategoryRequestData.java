package cgx.com.usecase.ManageCategory.UpdateCategory;

public class UpdateCategoryRequestData {
	public final String authToken;
    public final String categoryId; // ID của danh mục cần sửa
    public final String name;
    public final String description;
    public final String parentCategoryId; // ID danh mục cha mới (có thể null)
    
    public UpdateCategoryRequestData(String authToken, String categoryId, String name, String description, String parentCategoryId) {
        this.authToken = authToken;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
    }
}
