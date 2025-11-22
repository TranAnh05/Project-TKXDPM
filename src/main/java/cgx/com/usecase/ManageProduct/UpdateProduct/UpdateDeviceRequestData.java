package cgx.com.usecase.ManageProduct.UpdateProduct;

import java.math.BigDecimal;

public class UpdateDeviceRequestData {
	public final String authToken;
    public final String id; // ID sản phẩm cần sửa
    public final String name;
    public final String description;
    public final BigDecimal price;
    public final int stockQuantity;
    public final String categoryId;
    public final String thumbnail;
    public final String status; // Có thể cập nhật trạng thái (ACTIVE/OUT_OF_STOCK)

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
