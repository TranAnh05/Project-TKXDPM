package cgx.com.usecase.ManageProduct.UpdateProduct;

import java.math.BigDecimal;

public class UpdateMouseRequestData extends UpdateDeviceRequestData {
    public final int dpi;
    public final boolean isWireless;
    public final int buttonCount;

    public UpdateMouseRequestData(String authToken, String id, String name, String description, 
                                  BigDecimal price, int stockQuantity, String categoryId, 
                                  String thumbnail, String status,
                                  int dpi, boolean isWireless, int buttonCount) {
        super(authToken, id, name, description, price, stockQuantity, categoryId, thumbnail, status);
        this.dpi = dpi;
        this.isWireless = isWireless;
        this.buttonCount = buttonCount;
    }
}