package cgx.com.usecase.ManageProduct.UpdateProduct;

import java.time.Instant;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

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
            // 1. Validate Auth (Admin only)
            if (input.authToken == null || input.authToken.trim().isEmpty()) throw new SecurityException("Auth Token không được để trống.");
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            if (principal.role != UserRole.ADMIN) throw new SecurityException("Không có quyền truy cập (Yêu cầu Admin).");

            // 2. Validate Input ID
            ComputerDevice.validateId(input.id);

            // 3. Tìm dữ liệu cũ (DTO)
            DeviceData existingData = deviceRepository.findById(input.id);
            if (existingData == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + input.id);
            }

            // 4. Tái tạo Entity (Re-hydrate) - Lớp con thực hiện kiểm tra kiểu
            ComputerDevice deviceEntity = rehydrateEntity(existingData);
            if (deviceEntity == null) {
                 // Nếu rehydrate trả về null nghĩa là data trong DB không khớp với loại UseCase đang gọi
                 throw new IllegalArgumentException("Dữ liệu sản phẩm không hợp lệ hoặc không khớp loại thiết bị.");
            }

            // 5. Validate thông tin chung (Logic Entity)
            // (Giả sử ComputerDevice.validateCommonInfo check null name, price < 0, stock < 0)
            ComputerDevice.validateCommonInfo(input.name, input.price, input.stockQuantity);
            
            // 6. Validate thông tin riêng (Lớp con thực hiện)
            validateSpecificData(input);

            // 7. Check trùng tên (Chung) - Chỉ check nếu tên thay đổi
            if (!input.name.equalsIgnoreCase(deviceEntity.getName())) {
                if (deviceRepository.existsByName(input.name)) {
                    throw new IllegalArgumentException("Tên sản phẩm mới đã tồn tại.");
                }
            }

            // 8. Check Category tồn tại (Chung)
            if (categoryRepository.findById(input.categoryId) == null) {
                throw new IllegalArgumentException("Danh mục không tồn tại.");
            }

            // 9. Tạo Entity mới với thông tin đã cập nhật (Lớp con thực hiện)
            ComputerDevice updatedEntity = createUpdatedEntity(deviceEntity, input);

            // 10. Map Entity -> DTO để lưu
            // Bước 10a: Map chung
            DeviceData dataToSave = mapCommonData(updatedEntity);
            // Bước 10b: Map riêng (Ủy quyền cho lớp con -> OCP)
            mapSpecificDataToDTO(updatedEntity, dataToSave);
            
            // 11. Lưu
            deviceRepository.save(dataToSave);

            // 12. Success
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

    // --- Template Methods (Lớp con phải implement) ---
    protected abstract ComputerDevice rehydrateEntity(DeviceData data);
    protected abstract void validateSpecificData(T input);
    protected abstract ComputerDevice createUpdatedEntity(ComputerDevice oldEntity, T input);
    
    /**
     * Phương thức mới để đảm bảo OCP.
     * Lớp con tự biết cách lấy dữ liệu từ Entity của mình để nhét vào DeviceData DTO.
     */
    protected abstract void mapSpecificDataToDTO(ComputerDevice entity, DeviceData data);

    // --- Helper Mapper Chung ---
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