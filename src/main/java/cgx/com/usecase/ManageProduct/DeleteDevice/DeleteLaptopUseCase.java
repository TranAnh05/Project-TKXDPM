package cgx.com.usecase.ManageProduct.DeleteDevice;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Laptop;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

public class DeleteLaptopUseCase extends AbstractDeleteDeviceUseCase {

    public DeleteLaptopUseCase(IDeviceRepository deviceRepository,
    						   IOrderRepository orderRepository,
                               IAuthTokenValidator tokenValidator,
                               DeleteDeviceOutputBoundary outputBoundary) {
        super(deviceRepository, orderRepository, tokenValidator, outputBoundary);
    }

    @Override
    protected ComputerDevice rehydrateEntity(DeviceData data) {
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
