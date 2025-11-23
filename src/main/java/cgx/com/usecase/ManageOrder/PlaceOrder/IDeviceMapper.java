package cgx.com.usecase.ManageOrder.PlaceOrder;

import cgx.com.Entities.ComputerDevice;
import cgx.com.usecase.ManageProduct.DeviceData;

public interface IDeviceMapper {
	ComputerDevice toEntity(DeviceData data);
    DeviceData toDTO(ComputerDevice entity);
}
