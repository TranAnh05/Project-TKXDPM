package cgx.com.usecase.ManageOrder.PlaceOrder;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Mouse;
import cgx.com.usecase.ManageProduct.DeviceData;

public class MouseMapper implements IDeviceSpecificMapper {

    @Override
    public boolean supports(String type) {
        return "MOUSE".equalsIgnoreCase(type);
    }

    @Override
    public ComputerDevice toEntity(DeviceData data) {
        return new Mouse(
            data.id, data.name, data.description, data.price, data.stockQuantity,
            data.categoryId, data.status, data.thumbnail, data.createdAt, data.updatedAt,
            data.dpi, data.isWireless, data.buttonCount
        );
    }

    @Override
    public DeviceData toDTO(ComputerDevice entity) {
        Mouse m = (Mouse) entity;
        DeviceData data = new DeviceData();
        data.dpi = m.getDpi();
        data.isWireless = m.isWireless();
        data.buttonCount = m.getButtonCount();
        return data;
    }
}
