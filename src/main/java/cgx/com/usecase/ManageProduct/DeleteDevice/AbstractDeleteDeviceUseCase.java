package cgx.com.usecase.ManageProduct.DeleteDevice;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

public abstract class AbstractDeleteDeviceUseCase implements DeleteDeviceInputBoundary {

    protected final IDeviceRepository deviceRepository;
    protected final IAuthTokenValidator tokenValidator;
    protected final DeleteDeviceOutputBoundary outputBoundary;

    public AbstractDeleteDeviceUseCase(IDeviceRepository deviceRepository,
                                       IAuthTokenValidator tokenValidator,
                                       DeleteDeviceOutputBoundary outputBoundary) {
        this.deviceRepository = deviceRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public final void execute(DeleteDeviceRequestData input) {
        DeleteDeviceResponseData output = new DeleteDeviceResponseData();

        try {
            // 1. Validate Auth (Admin only)
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            if (principal.role != UserRole.ADMIN) {
                throw new SecurityException("Không có quyền truy cập (Yêu cầu Admin).");
            }

            // 2. Validate ID
            ComputerDevice.validateId(input.deviceId);

            // 3. Tìm thiết bị (DTO)
            DeviceData deviceData = deviceRepository.findById(input.deviceId);
            if (deviceData == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + input.deviceId);
            }

            // 4. Tái tạo Entity (Abstract - Lớp con thực hiện)
            ComputerDevice deviceEntity = rehydrateEntity(deviceData);
            
            if (deviceEntity == null) {
                throw new IllegalArgumentException("Dữ liệu sản phẩm không hợp lệ hoặc không khớp loại thiết bị.");
            }

            // 5. Thực hiện Logic Xóa Mềm trên Entity
            deviceEntity.softDelete();

            // 6. Map ngược lại DTO để lưu
            // 6a. Map chung
            DeviceData dataToSave = mapCommonData(deviceEntity);
            // 6b. Map riêng (Abstract - Lớp con thực hiện)
            mapSpecificDataToDTO(deviceEntity, dataToSave);

            // 7. Lưu vào DB
            deviceRepository.save(dataToSave);

            // 8. Thành công
            output.success = true;
            output.message = "Xóa sản phẩm thành công.";
            output.deletedDeviceId = deviceEntity.getId();
            output.newStatus = deviceEntity.getStatus();

        } catch (IllegalArgumentException | SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống không xác định: " + e.getMessage();
        }

        outputBoundary.present(output);
    }

    // --- Template Methods ---
    protected abstract ComputerDevice rehydrateEntity(DeviceData data);
    protected abstract void mapSpecificDataToDTO(ComputerDevice entity, DeviceData data);

    // --- Helper ---
    private DeviceData mapCommonData(ComputerDevice entity) {
        DeviceData data = new DeviceData();
        data.id = entity.getId();
        data.name = entity.getName();
        data.description = "Desc"; 
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