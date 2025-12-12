package cgx.com.usecase.ManageProduct.UpdateProduct;

import java.time.Instant;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Mouse;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

public class UpdateMouseUseCase extends AbstractUpdateDeviceUseCase<UpdateMouseRequestData> {

    public UpdateMouseUseCase(IDeviceRepository deviceRepository,
                              ICategoryRepository categoryRepository,
                              IAuthTokenValidator tokenValidator,
                              UpdateDeviceOutputBoundary outputBoundary) {
        super(deviceRepository, categoryRepository, tokenValidator, outputBoundary);
    }

    @Override
    protected ComputerDevice rehydrateEntity(DeviceData data) {
        return new Mouse(
            data.id, data.name, data.description, data.price, data.stockQuantity,
            data.categoryId, data.status, data.thumbnail, data.createdAt, data.updatedAt,
            data.dpi, data.isWireless, data.buttonCount
        );
    }

    @Override
    protected void validateSpecificData(UpdateMouseRequestData input) {
        Mouse.validateSpecs(input.dpi, input.buttonCount);
    }

    @Override
    protected ComputerDevice createUpdatedEntity(ComputerDevice oldEntity, UpdateMouseRequestData input) {
        return new Mouse(
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
            input.dpi,
            input.isWireless,
            input.buttonCount
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