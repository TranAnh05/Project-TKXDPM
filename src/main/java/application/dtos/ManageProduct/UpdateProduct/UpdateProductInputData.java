package application.dtos.ManageProduct.UpdateProduct;

import java.util.Map;

public class UpdateProductInputData {
	public int id; // ID của sản phẩm cần sửa
    // (Các thuộc tính chung)
    public String name;
    public String description;
    public double price;
    public int stockQuantity;
    public String imageUrl;
    public int categoryId; // (Không cho phép đổi Category)
    // (Thuộc tính riêng)
    public Map<String, String> attributes;
    
    public UpdateProductInputData() {
    	
    }
    
	public UpdateProductInputData(int id, String name, String description, double price, int stockQuantity,
			String imageUrl, int categoryId, Map<String, String> attributes) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.stockQuantity = stockQuantity;
		this.imageUrl = imageUrl;
		this.categoryId = categoryId;
		this.attributes = attributes;
	} 
}	
