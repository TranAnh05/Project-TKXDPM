package cgx.com.usecase.ManageProduct.DeleteDevice;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Mouse;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

public class DeleteMouseUseCase extends AbstractDeleteDeviceUseCase {

    public DeleteMouseUseCase(IDeviceRepository deviceRepository,
                              IAuthTokenValidator tokenValidator,
                              DeleteDeviceOutputBoundary outputBoundary) {
        super(deviceRepository, tokenValidator, outputBoundary);
    }

    @Override
    protected ComputerDevice rehydrateEntity(DeviceData data) {
        if (data.dpi == null) return null; // Không phải Mouse
        
        return new Mouse(
            data.id, data.name, data.description, data.price, data.stockQuantity,
            data.categoryId, data.status, data.thumbnail, data.createdAt, data.updatedAt,
            data.dpi, data.isWireless, data.buttonCount
        );
    }

    @Override
    protected void mapSpecificDataToDTO(ComputerDevice entity, DeviceData data) {
        if (entity instanceof Mouse) {
            Mouse m = (Mouse) entity;
            data.dpi = m.getDpi();
            data.isWireless = m.isWireless();
            data.buttonCount = m.getButtonCount();
        }
    }
}