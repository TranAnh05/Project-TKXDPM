package cgx.com.usecase.ManageProduct.AddNewProduct;

import java.math.BigDecimal;

public class AddDeviceRequestData {
	public String authToken;
    public String name;
    public String description;
    public BigDecimal price;
    public int stockQuantity;
    public String categoryId;
    public String thumbnail;

    public AddDeviceRequestData(String authToken, String name, String description, 
                                BigDecimal price, int stockQuantity, String categoryId, String thumbnail) {
        this.authToken = authToken;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.categoryId = categoryId;
        this.thumbnail = thumbnail;
    }
}
