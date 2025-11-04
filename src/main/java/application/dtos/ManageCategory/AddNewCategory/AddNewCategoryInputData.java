package application.dtos.ManageCategory.AddNewCategory;

public class AddNewCategoryInputData {
	public String name;
	public String attributeTemplate;
	
	public AddNewCategoryInputData() {
		
	}
	
	public AddNewCategoryInputData(String name, String attributeTemplate) {
		this.name = name;
		this.attributeTemplate = attributeTemplate;
	}
}
