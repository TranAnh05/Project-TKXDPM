package cgx.com.usecase.ManageProduct.AddNewProduct;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

/**
 * Use Case TRỪU TƯỢNG: Thêm Thiết Bị.
 * Xử lý luồng chung: Auth -> Validate Chung -> Validate Riêng -> Tạo Entity -> Lưu.
 */
public abstract class AbstractAddDeviceUseCase<T extends AddDeviceRequestData> implements AddDeviceInputBoundary<T> {

    protected final IDeviceRepository deviceRepository;
    protected final ICategoryRepository categoryRepository; // Để check category tồn tại
    protected final IAuthTokenValidator tokenValidator;
    protected final IUserIdGenerator idGenerator; // Dùng chung generator ID
    protected final AddDeviceOutputBoundary outputBoundary;

    public AbstractAddDeviceUseCase(IDeviceRepository deviceRepository,
                                    ICategoryRepository categoryRepository,
                                    IAuthTokenValidator tokenValidator,
                                    IUserIdGenerator idGenerator,
                                    AddDeviceOutputBoundary outputBoundary) {
        this.deviceRepository = deviceRepository;
        this.categoryRepository = categoryRepository;
        this.tokenValidator = tokenValidator;
        this.idGenerator = idGenerator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public final void execute(T input) {
        AddDeviceResponseData output = new AddDeviceResponseData();

        try {
            // 1. Validate Quyền Admin (Chung)
            if (input.authToken == null || input.authToken.trim().isEmpty()) throw new SecurityException("Auth Token không được để trống.");
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            if (principal.role != UserRole.ADMIN) throw new SecurityException("Không có quyền truy cập (Yêu cầu Admin).");

            // 2. Validate thông tin chung (Entity)
            ComputerDevice.validateCommonInfo(input.name, input.price, input.stockQuantity);

            // 3. Check trùng tên (Chung)
            if (deviceRepository.existsByName(input.name)) {
                throw new IllegalArgumentException("Tên sản phẩm đã tồn tại.");
            }

            // 4. Check Category tồn tại (Chung)
            if (categoryRepository.findById(input.categoryId) == null) {
                throw new IllegalArgumentException("Danh mục không tồn tại.");
            }

            // 5. Validate thông tin riêng (Lớp con thực hiện)
            validateSpecificData(input);

            // 6. Tạo Entity (Lớp con thực hiện)
            String newId = idGenerator.generate();
            ComputerDevice deviceEntity = createEntity(input, newId);

            // 7. Map Entity -> Data DTO
            // Bước 7a: Map các trường chung (Lớp cha làm)
            DeviceData dataToSave = mapCommonData(deviceEntity);
            
            // Bước 7b: Map các trường riêng (Lớp con làm - OCP)
            mapSpecificDataToDTO(deviceEntity, dataToSave);

            // 8. Lưu vào DB
            deviceRepository.save(dataToSave);

            // 9. Thành công
            output.success = true;
            output.message = "Thêm sản phẩm thành công!";
            output.newDeviceId = newId;

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
    protected abstract void validateSpecificData(T input);
    protected abstract ComputerDevice createEntity(T input, String newId);
    
    /**
     * Phương thức mới để đảm bảo OCP.
     * Lớp con tự biết cách lấy dữ liệu từ Entity của mình để nhét vào DeviceData DTO.
     */
    protected abstract void mapSpecificDataToDTO(ComputerDevice entity, DeviceData data);

    // --- Helper mapping chung ---
    private DeviceData mapCommonData(ComputerDevice entity) {
        DeviceData data = new DeviceData();
        data.id = entity.getId();
        data.name = entity.getName();
        data.description = "Mô tả sản phẩm"; // Giả lập hoặc lấy từ entity nếu có getter
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