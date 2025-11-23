package cgx.com.usecase.ManageOrder.PlaceOrder;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Laptop;
import cgx.com.usecase.ManageProduct.DeviceData;

public class LaptopMapper implements IDeviceSpecificMapper {

    @Override
    public boolean supports(String type) {
        return "LAPTOP".equalsIgnoreCase(type);
    }

    @Override
    public ComputerDevice toEntity(DeviceData data) {
        return new Laptop(
            data.id, data.name, data.description, data.price, data.stockQuantity,
            data.categoryId, data.status, data.thumbnail, data.createdAt, data.updatedAt,
            data.cpu, data.ram, data.storage, data.screenSize
        );
    }

    @Override
    public DeviceData toDTO(ComputerDevice entity) {
        Laptop l = (Laptop) entity;
        DeviceData data = new DeviceData();
        data.cpu = l.getCpu();
        data.ram = l.getRam();
        data.storage = l.getStorage();
        data.screenSize = l.getScreenSize();
        // (Các field chung sẽ được map ở lớp cha DeviceMapper để tránh lặp code)
        return data;
    }
}