package application.dtos.ManageCategory.UpdateCategory;

public class UpdateCategoryInputData {
	public int id;
	public String name;
	public String attributeTemplate;

	public UpdateCategoryInputData(int id, String name, String attributeTemplate) {
		this.id = id;
		this.name = name;
		this.attributeTemplate = attributeTemplate;
	}
}
