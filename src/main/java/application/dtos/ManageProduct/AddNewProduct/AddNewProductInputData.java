package application.dtos.ManageProduct.AddNewProduct;

import java.util.Map;

public class AddNewProductInputData {
	public String name;
    public String description;
    public double price;
    public int stockQuantity;
    public String imageUrl;
    public int categoryId;
    
    // Thuộc tính riêng (ví dụ: {"cpu":"i7", "ram":"16"})
    public Map<String, String> attributes;
    
    public AddNewProductInputData(String name, String description, double price, int stockQuantity, 
            String imageUrl, int categoryId, Map<String, String> attributes) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.stockQuantity = stockQuantity;
		this.imageUrl = imageUrl;
		this.categoryId = categoryId;
		this.attributes = attributes;
	}
}
