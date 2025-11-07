package infrastructure.web.requests.UpdateProduct;

import java.util.Map;

public class UpdateProductRequest {
	// Thuộc tính chung
    public String name;
    public String description;
    public double price;
    public int stockQuantity;
    public String imageUrl;
    public int categoryId; // (Giả sử không cho phép đổi Category khi Sửa)
    
    // Thuộc tính riêng
    public Map<String, String> attributes;
}
