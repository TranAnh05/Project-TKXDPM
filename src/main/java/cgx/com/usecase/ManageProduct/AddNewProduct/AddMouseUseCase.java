package cgx.com.usecase.ManageProduct.AddNewProduct;

import java.time.Instant;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Mouse;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.IProductIdGenerator;
import cgx.com.usecase.ManageUser.IUserIdGenerator;

public class AddMouseUseCase extends AbstractAddDeviceUseCase<AddMouseRequestData> {

    public AddMouseUseCase(IDeviceRepository deviceRepository,
                           ICategoryRepository categoryRepository,
                           IAuthTokenValidator tokenValidator,
                           IProductIdGenerator idGenerator,
                           AddDeviceOutputBoundary outputBoundary) {
        super(deviceRepository, categoryRepository, tokenValidator, idGenerator, outputBoundary);
    }

    @Override
    protected void validateSpecificData(AddMouseRequestData input) {
        Mouse.validateSpecs(input.dpi, input.buttonCount);
    }

    @Override
    protected ComputerDevice createEntity(AddMouseRequestData input, String newId) {
        return new Mouse(
            newId, input.name, input.description, input.price, input.stockQuantity, 
            input.categoryId, "AVAILABLE", input.thumbnail, Instant.now(), Instant.now(),
            input.dpi, input.isWireless, input.buttonCount
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
