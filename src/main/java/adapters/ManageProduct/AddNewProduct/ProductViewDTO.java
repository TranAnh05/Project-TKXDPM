package adapters.ManageProduct.AddNewProduct;

public class ProductViewDTO {
	public String id;
    public String name;
    public String description;
    public String price; // <-- String
    public String stockQuantity; // <-- String
    public String imageUrl;
    public String categoryId; // <-- String
    public String categoryName;
    
    // Thuộc tính Laptop
    public String cpu;
    public String ram; // <-- String
    public String screenSize;
    
    // Thuộc tính Mouse
    public String connectionType;
    public String dpi; // <-- String
    
    // Thuộc tính Keyboard
    public String switchType;
    public String layout;
}
