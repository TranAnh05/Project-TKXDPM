package Entities;

import java.util.Map;

public abstract class Product {
	protected int id;
	protected String name;
	protected String description;
	protected double price;
	protected int stockQuantity;
	protected String imageUrl;
	protected int categoryId;
    
    public Product(String name, String description, double price, int stockQuantity, String imageUrl, int categoryId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
    }
    
    public Product(int id, String name, String description, double price, int stockQuantity, String imageUrl, int categoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public String getImageUrl() { return imageUrl; }
    public int getCategoryId() { return categoryId; }
    
    public static void validateCommon(String name, String description, double price, int stock) {
    	if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
    	
    	if (description == null || description.trim().isEmpty()) {
    		throw new IllegalArgumentException("Mô tả sản phẩm không được để trống.");
    	}
    	
    	if (price < 0) {
            throw new IllegalArgumentException("Giá sản phẩm không được âm.");
        }
    	
    	if (stock < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm.");
        }
    }
   
    public void addStock(int quantityToAdd) {
        if ((this.stockQuantity + quantityToAdd) < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không thể là số âm.");
        }
        
        this.stockQuantity += quantityToAdd;
    }
    
    public void updateCommon(String name, String description, double price, int stockQuantity, String imageUrl) {
        // 1. Gọi validation (static)
    	validateCommon(name, description, price, stockQuantity);
        // 2. Gán giá trị
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
    }
    
    public abstract void updateSpecific(Map<String, String> attributes);
    
    public abstract Map<String, String> getSpecificAttributes();
}
