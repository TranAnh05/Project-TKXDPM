package cgx.com.usecase.ManageCategory.AddNewCategory;

public class AddCategoryRequestData {
	public final String authToken;
    public final String name;
    public final String description;
    public final String parentCategoryId; // Có thể null nếu là Root Category

    public AddCategoryRequestData(String authToken, String name, String description, String parentCategoryId) {
        this.authToken = authToken;
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
    }
}
