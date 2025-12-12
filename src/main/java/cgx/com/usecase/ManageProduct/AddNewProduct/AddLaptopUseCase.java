package cgx.com.usecase.ManageProduct.AddNewProduct;

import java.time.Instant;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Laptop;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.IProductIdGenerator;
import cgx.com.usecase.ManageUser.IUserIdGenerator;

public class AddLaptopUseCase extends AbstractAddDeviceUseCase<AddLaptopRequestData> {

    public AddLaptopUseCase(IDeviceRepository deviceRepository,
                            ICategoryRepository categoryRepository,
                            IAuthTokenValidator tokenValidator,
                            IProductIdGenerator idGenerator,
                            AddDeviceOutputBoundary outputBoundary) {
        super(deviceRepository, categoryRepository, tokenValidator, idGenerator, outputBoundary);
    }

    @Override
    protected void validateSpecificData(AddLaptopRequestData input) {
        Laptop.validateSpecs(input.cpu, input.ram, input.storage, input.screenSize);
    }

    @Override
    protected ComputerDevice createEntity(AddLaptopRequestData input, String newId) {
        return new Laptop(
            newId, input.name, input.description, input.price, input.stockQuantity, 
            input.categoryId, "AVAILABLE", input.thumbnail, Instant.now(), Instant.now(),
            input.cpu, input.ram, input.storage, input.screenSize
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