package application.dtos.ManageProduct;

public class ProductData {
	public int id;
    public String name;
    public String description;
    public double price;
    public int stockQuantity;
    public String imageUrl;
    public int categoryId;
    
    // Thuộc tính Laptop
    public String cpu;
    public int ram;
    public String screenSize;
    
    // Thuộc tính Mouse
    public String connectionType;
    public int dpi;
    
    // THUỘC TÍNH Keyboard
    public String switchType;
    public String layout;
    
    public ProductData() {
    	
    }

	public ProductData(int id, String name, String description, double price, int stockQuantity, String imageUrl,
			int categoryId, String cpu, int ram, String screenSize, String connectionType, int dpi, String switchType,
			String layout) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.stockQuantity = stockQuantity;
		this.imageUrl = imageUrl;
		this.categoryId = categoryId;
		this.cpu = cpu;
		this.ram = ram;
		this.screenSize = screenSize;
		this.connectionType = connectionType;
		this.dpi = dpi;
		this.switchType = switchType;
		this.layout = layout;
	}
    
    
}
