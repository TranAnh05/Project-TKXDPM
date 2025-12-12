package cgx.com.usecase.ManageCategory.AddNewCategory;

public class AddCategoryRequestData {
	public String authToken;
    public String name;
    public String description;
    public String parentCategoryId; // Có thể null nếu là Root Category

    public AddCategoryRequestData(String authToken, String name, String description, String parentCategoryId) {
        this.authToken = authToken;
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
    }

	public AddCategoryRequestData() {
	}
}
