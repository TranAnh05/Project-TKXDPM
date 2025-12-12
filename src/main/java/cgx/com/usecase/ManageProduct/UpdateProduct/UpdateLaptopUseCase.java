package cgx.com.usecase.ManageProduct.UpdateProduct;

import java.time.Instant;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Laptop;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

public class UpdateLaptopUseCase extends AbstractUpdateDeviceUseCase<UpdateLaptopRequestData> {

    public UpdateLaptopUseCase(IDeviceRepository deviceRepository,
                               ICategoryRepository categoryRepository,
                               IAuthTokenValidator tokenValidator,
                               UpdateDeviceOutputBoundary outputBoundary) {
        super(deviceRepository, categoryRepository, tokenValidator, outputBoundary);
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
    protected void validateSpecificData(UpdateLaptopRequestData input) {
        Laptop.validateSpecs(input.cpu, input.ram, input.storage, input.screenSize);
    }

    @Override
    protected ComputerDevice createUpdatedEntity(ComputerDevice oldEntity, UpdateLaptopRequestData input) {
        return new Laptop(
            oldEntity.getId(),
            input.name,
            input.description,
            input.price,
            input.stockQuantity,
            input.categoryId,
            input.status,
            input.thumbnail,
            oldEntity.getCreatedAt(),
            Instant.now(), 
            input.cpu,
            input.ram,
            input.storage,
            input.screenSize
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
