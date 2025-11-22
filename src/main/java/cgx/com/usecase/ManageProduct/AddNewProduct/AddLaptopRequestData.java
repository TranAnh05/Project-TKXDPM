package cgx.com.usecase.ManageProduct.AddNewProduct;

import java.math.BigDecimal;

public class AddLaptopRequestData extends AddDeviceRequestData {
    public final String cpu;
    public final String ram;
    public final String storage;
    public final double screenSize;

    public AddLaptopRequestData(String authToken, String name, String description, BigDecimal price, int stockQuantity, 
                                String categoryId, String thumbnail, String cpu, String ram, String storage, double screenSize) {
        super(authToken, name, description, price, stockQuantity, categoryId, thumbnail);
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.screenSize = screenSize;
    }
}