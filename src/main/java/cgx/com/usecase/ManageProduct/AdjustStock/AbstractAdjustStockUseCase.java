package cgx.com.usecase.ManageProduct.AdjustStock;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public abstract class AbstractAdjustStockUseCase implements AdjustStockInputBoundary {

    protected final IDeviceRepository deviceRepository;
    protected final IAuthTokenValidator tokenValidator;
    protected final AdjustStockOutputBoundary outputBoundary;

    public AbstractAdjustStockUseCase(IDeviceRepository deviceRepository,
                                      IAuthTokenValidator tokenValidator,
                                      AdjustStockOutputBoundary outputBoundary) {
        this.deviceRepository = deviceRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public final void execute(AdjustStockRequestData input) {
        AdjustStockResponseData output = new AdjustStockResponseData();

        try {
            // 1. Validate Auth (Admin only)
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            if (principal.role != UserRole.ADMIN) {
                throw new SecurityException("Không có quyền truy cập (Yêu cầu Admin).");
            }

            // 2. Validate Input ID
            if (input.deviceId == null || input.deviceId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID sản phẩm không được để trống.");
            }

            // 3. Tìm thiết bị (Lấy DTO từ DB)
            DeviceData deviceData = deviceRepository.findById(input.deviceId);
            if (deviceData == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + input.deviceId);
            }

            // 4. Tái tạo Entity (Abstract - Lớp con thực hiện)
            // Lớp con sẽ quyết định xem DTO này có phải là loại của nó không.
            ComputerDevice deviceEntity = rehydrateEntity(deviceData);
            
            if (deviceEntity == null) {
                throw new IllegalArgumentException("Dữ liệu sản phẩm không hợp lệ hoặc không khớp loại thiết bị.");
            }

            // 5. Thực hiện Logic Nghiệp vụ trên Entity (Entity tự validate số lượng âm)
            deviceEntity.updateStock(input.newQuantity);

            // 6. Chuyển ngược lại DTO để lưu
            // 6a. Map chung
            DeviceData dataToSave = mapCommonData(deviceEntity);
            // 6b. Map riêng (Abstract - Lớp con thực hiện)
            mapSpecificDataToDTO(deviceEntity, dataToSave);

            // 7. Lưu vào DB
            deviceRepository.save(dataToSave);

            // 8. Thành công
            output.success = true;
            output.message = "Cập nhật tồn kho thành công.";
            output.currentStock = deviceEntity.getStockQuantity();

        } catch (IllegalArgumentException | SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống không xác định: " + e.getMessage();
        }

        outputBoundary.present(output);
    }

    // --- Template Methods (Lớp con phải implement) ---
    
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