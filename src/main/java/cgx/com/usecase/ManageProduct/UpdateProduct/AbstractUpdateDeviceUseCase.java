package cgx.com.usecase.ManageProduct.UpdateProduct;

import java.time.Instant;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

/**
 * Use Case TRỪU TƯỢNG cho Update.
 * Tuân thủ OCP và Template Method.
 */
public abstract class AbstractUpdateDeviceUseCase<T extends UpdateDeviceRequestData> implements UpdateDeviceInputBoundary<T> {

    protected final IDeviceRepository deviceRepository;
    protected final ICategoryRepository categoryRepository;
    protected final IAuthTokenValidator tokenValidator;
    protected final UpdateDeviceOutputBoundary outputBoundary;

    public AbstractUpdateDeviceUseCase(IDeviceRepository deviceRepository,
                                       ICategoryRepository categoryRepository,
                                       IAuthTokenValidator tokenValidator,
                                       UpdateDeviceOutputBoundary outputBoundary) {
        this.deviceRepository = deviceRepository;
        this.categoryRepository = categoryRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public final void execute(T input) {
        UpdateDeviceResponseData output = new UpdateDeviceResponseData();

        try {
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            User.validateIsAdmin(principal.role);
            
            ComputerDevice.validateId(input.id);
            ComputerDevice.validateCommonInfo(input.name, input.description, input.price, input.stockQuantity);
            ComputerDevice.validateNewStatus(input.status);
            validateSpecificData(input);

            DeviceData existingData = deviceRepository.findById(input.id);
            if (existingData == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + input.id);
            }

            ComputerDevice deviceEntity = rehydrateEntity(existingData);

            if (!input.name.equalsIgnoreCase(deviceEntity.getName())) {
                if (deviceRepository.existsByName(input.name)) {
                    throw new IllegalArgumentException("Tên sản phẩm mới đã tồn tại.");
                }
            }
            
            if (categoryRepository.findById(input.categoryId) == null) {
                throw new IllegalArgumentException("Danh mục không tồn tại.");
            }

            ComputerDevice updatedEntity = createUpdatedEntity(deviceEntity, input);

            DeviceData dataToSave = mapCommonData(updatedEntity);
            
            mapSpecificDataToDTO(updatedEntity, dataToSave);
            
            deviceRepository.save(dataToSave);

            output.success = true;
            output.message = "Cập nhật sản phẩm thành công!";
            output.deviceId = updatedEntity.getId();

        } catch (IllegalArgumentException | SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống không xác định: " + e.getMessage();
        }

        outputBoundary.present(output);
    }

    protected abstract ComputerDevice rehydrateEntity(DeviceData data);
    protected abstract void validateSpecificData(T input);
    protected abstract ComputerDevice createUpdatedEntity(ComputerDevice oldEntity, T input);
    protected abstract void mapSpecificDataToDTO(ComputerDevice entity, DeviceData data);

    private DeviceData mapCommonData(ComputerDevice entity) {
        DeviceData data = new DeviceData();
        data.id = entity.getId();
        data.name = entity.getName();
        data.description = entity.getDescription(); 
        data.price = entity.getPrice();
        data.stockQuantity = entity.getStockQuantity();
        data.categoryId = entity.getCategoryId();
        data.status = entity.getStatus();
        data.thumbnail = entity.getThumbnail();
        data.createdAt = entity.getCreatedAt();
        data.updatedAt = entity.getUpdatedAt();
        return data;
    }
}