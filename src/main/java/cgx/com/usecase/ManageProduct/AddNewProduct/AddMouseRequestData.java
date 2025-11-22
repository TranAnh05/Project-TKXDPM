package cgx.com.usecase.ManageProduct.AddNewProduct;

import java.math.BigDecimal;

public class AddMouseRequestData extends AddDeviceRequestData {
    public final int dpi;
    public final boolean isWireless;
    public final int buttonCount;

    public AddMouseRequestData(String authToken, String name, String description, BigDecimal price, int stockQuantity, 
                               String categoryId, String thumbnail, int dpi, boolean isWireless, int buttonCount) {
        super(authToken, name, description, price, stockQuantity, categoryId, thumbnail);
        this.dpi = dpi;
        this.isWireless = isWireless;
        this.buttonCount = buttonCount;
    }
}