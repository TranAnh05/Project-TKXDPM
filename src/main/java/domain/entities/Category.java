package domain.entities;

public class Category {
	private int id;
    private String name;
    private String attributeTemplate;
    
    public Category() {
    	
    }
    
    public Category(String name, String attributeTemplate) {
        this.name = name;
        this.attributeTemplate = attributeTemplate;
    }
    
    public Category(int id, String name, String attributeTemplate) {
        this.id = id;
        this.name = name;
        this.attributeTemplate = attributeTemplate;
    }
    
    public static void isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại sản phẩm không được để trống.");
        }
    }
    
    public static void isValidTemplate(String template) {
        if (template == null) {
            throw new IllegalArgumentException("Bản mẫu thuộc tính không được rỗng.");
        }
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAttributeTemplate() { return attributeTemplate; }
    
    public void setName(String name) { this.name = name; }
    public void setAttributeTemplate(String template) { this.attributeTemplate = template; }
    
    public void updateInfo(String newName, String newTemplate) {
        this.name = newName;
        this.attributeTemplate = newTemplate;
    }
}
