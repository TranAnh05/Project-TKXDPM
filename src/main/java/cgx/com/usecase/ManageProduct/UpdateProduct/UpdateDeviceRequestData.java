package cgx.com.usecase.ManageProduct.UpdateProduct;

import java.math.BigDecimal;

public class UpdateDeviceRequestData {
	public String authToken;
    public String id; 
    public String name;
    public String description;
    public BigDecimal price;
    public int stockQuantity;
    public String categoryId;
    public String thumbnail;
    public String status; 

    public UpdateDeviceRequestData(String authToken, String id, String name, String description, 
                                   BigDecimal price, int stockQuantity, String categoryId, 
                                   String thumbnail, String status) {
        this.authToken = authToken;
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.categoryId = categoryId;
        this.thumbnail = thumbnail;
        this.status = status;
    }
}
