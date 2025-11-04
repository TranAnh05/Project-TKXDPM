package application.dtos.ManageCategory;

public class CategoryData {
	public int id;
    public String name;
    public String attributeTemplate;
    
    public CategoryData() {
    	
    }
    
	public CategoryData(int id, String name, String attributeTemplate) {
		this.id = id;
		this.name = name;
		this.attributeTemplate = attributeTemplate;
	}
}
