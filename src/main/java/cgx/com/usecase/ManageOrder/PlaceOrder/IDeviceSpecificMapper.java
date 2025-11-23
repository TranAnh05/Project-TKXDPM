package cgx.com.usecase.ManageOrder.PlaceOrder;

import cgx.com.Entities.ComputerDevice;
import cgx.com.usecase.ManageProduct.DeviceData;

/**
 * Strategy Interface.
 * Mỗi loại thiết bị sẽ có một class implement interface này.
 */
public interface IDeviceSpecificMapper {
	 // Hàm kiểm tra xem Mapper này có hỗ trợ loại dữ liệu này không
    boolean supports(String type);
    
    ComputerDevice toEntity(DeviceData data);
    DeviceData toDTO(ComputerDevice entity);
}
