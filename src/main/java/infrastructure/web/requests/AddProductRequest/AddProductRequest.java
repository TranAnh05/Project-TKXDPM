package infrastructure.web.requests.AddProductRequest;

import java.util.Map;

public class AddProductRequest {
	// Thuộc tính chung
    public String name;
    public String description;
    public double price;
    public int stockQuantity;
    public String imageUrl;
    public int categoryId;
    
    // Thuộc tính riêng
    public Map<String, String> attributes;
}
