package cgx.com.usecase.ManageProduct.DeleteDevice;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Laptop;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

public class DeleteLaptopUseCase extends AbstractDeleteDeviceUseCase {

    public DeleteLaptopUseCase(IDeviceRepository deviceRepository,
                               IAuthTokenValidator tokenValidator,
                               DeleteDeviceOutputBoundary outputBoundary) {
        super(deviceRepository, tokenValidator, outputBoundary);
    }

    @Override
    protected ComputerDevice rehydrateEntity(DeviceData data) {
        if (data.cpu == null) return null; // Không phải Laptop
        
        return new Laptop(
            data.id, data.name, data.description, data.price, data.stockQuantity,
            data.categoryId, data.status, data.thumbnail, data.createdAt, data.updatedAt,
            data.cpu, data.ram, data.storage, data.screenSize
        );
    }

    @Override
    protected void mapSpecificDataToDTO(ComputerDevice entity, DeviceData data) {
        if (entity instanceof Laptop) {
            Laptop l = (Laptop) entity;
            data.cpu = l.getCpu();
            data.ram = l.getRam();
            data.storage = l.getStorage();
            data.screenSize = l.getScreenSize();
        }
    }
}
