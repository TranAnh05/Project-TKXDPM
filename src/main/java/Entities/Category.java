package Entities;

public class Category {
	private int id;
    private String name;
    
    public Category() {
    	
    }
    
    public Category(String name) {
        this.name = name;
    }
    
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public static void isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại sản phẩm không được để trống.");
        }
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    
    public void setName(String name) { this.name = name; }
}
