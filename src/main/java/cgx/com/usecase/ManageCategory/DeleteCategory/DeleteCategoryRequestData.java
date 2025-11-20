package cgx.com.usecase.ManageCategory.DeleteCategory;

public class DeleteCategoryRequestData {
	public final String authToken;
    public final String categoryId; // ID danh mục cần xóa

    public DeleteCategoryRequestData(String authToken, String categoryId) {
        this.authToken = authToken;
        this.categoryId = categoryId;
    }
}
