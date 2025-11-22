package cgx.com.usecase.ManageProduct.UpdateProduct;

import java.math.BigDecimal;

public class UpdateLaptopRequestData extends UpdateDeviceRequestData {
    public final String cpu;
    public final String ram;
    public final String storage;
    public final double screenSize;

    public UpdateLaptopRequestData(String authToken, String id, String name, String description, 
                                   BigDecimal price, int stockQuantity, String categoryId, 
                                   String thumbnail, String status,
                                   String cpu, String ram, String storage, double screenSize) {
        super(authToken, id, name, description, price, stockQuantity, categoryId, thumbnail, status);
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.screenSize = screenSize;
    }
}