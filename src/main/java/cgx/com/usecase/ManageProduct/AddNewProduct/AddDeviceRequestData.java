package cgx.com.usecase.ManageProduct.AddNewProduct;

import java.math.BigDecimal;

public class AddDeviceRequestData {
	public final String authToken;
    public final String name;
    public final String description;
    public final BigDecimal price;
    public final int stockQuantity;
    public final String categoryId;
    public final String thumbnail;

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
