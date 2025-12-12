package cgx.com.usecase.ManageProduct.AddNewProduct;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.IProductIdGenerator;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;

public abstract class AbstractAddDeviceUseCase<T extends AddDeviceRequestData> implements AddDeviceInputBoundary<T> {

    protected final IDeviceRepository deviceRepository;
    protected final ICategoryRepository categoryRepository; 
    protected final IAuthTokenValidator tokenValidator;
    protected final IProductIdGenerator idGenerator; 
    protected final AddDeviceOutputBoundary outputBoundary;

    public AbstractAddDeviceUseCase(IDeviceRepository deviceRepository,
                                    ICategoryRepository categoryRepository,
                                    IAuthTokenValidator tokenValidator,
                                    IProductIdGenerator idGenerator,
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
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            User.validateIsAdmin(principal.role);
            
            ComputerDevice.validateCommonInfo(input.name, input.description, input.price, input.stockQuantity);
            validateSpecificData(input);

            if (deviceRepository.existsByName(input.name)) {
                throw new IllegalArgumentException("Tên sản phẩm đã tồn tại.");
            }

            if (categoryRepository.findById(input.categoryId) == null) {
                throw new IllegalArgumentException("Danh mục không tồn tại.");
            }

            String newId = idGenerator.generate();
            ComputerDevice deviceEntity = createEntity(input, newId);

            DeviceData dataToSave = mapCommonData(deviceEntity);
            
            mapSpecificDataToDTO(deviceEntity, dataToSave);

            deviceRepository.save(dataToSave);

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

    protected abstract void validateSpecificData(T input);
    protected abstract ComputerDevice createEntity(T input, String newId);
    
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