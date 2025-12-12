package cgx.com.usecase.ManageCategory.DeleteCategory;

public class DeleteCategoryRequestData {
    public String categoryId; // ID danh mục cần xóa
    public String authToken;

    public DeleteCategoryRequestData(String authToken, String categoryId) {
        this.authToken = authToken;
        this.categoryId = categoryId;
    }

	public DeleteCategoryRequestData() {
		// TODO Auto-generated constructor stub
	}
}
