package cgx.com.usecase.ManageProduct.DeleteDevice;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.User;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

public abstract class AbstractDeleteDeviceUseCase implements DeleteDeviceInputBoundary {

    protected final IDeviceRepository deviceRepository;
    protected final IOrderRepository orderRepository;
    protected final IAuthTokenValidator tokenValidator;
    protected final DeleteDeviceOutputBoundary outputBoundary;

    public AbstractDeleteDeviceUseCase(IDeviceRepository deviceRepository,
    								   IOrderRepository orderRepository,
                                       IAuthTokenValidator tokenValidator,
                                       DeleteDeviceOutputBoundary outputBoundary) {
        this.deviceRepository = deviceRepository;
        this.orderRepository = orderRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public final void execute(DeleteDeviceRequestData input) {
        DeleteDeviceResponseData output = new DeleteDeviceResponseData();

        try {
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            User.validateIsAdmin(principal.role);

            ComputerDevice.validateId(input.deviceId);

            DeviceData deviceData = deviceRepository.findById(input.deviceId);
            if (deviceData == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + input.deviceId);
            }
            
            if (orderRepository.isProductInActiveOrder(input.deviceId)) {
                throw new IllegalStateException("Không thể xóa. Sản phẩm đang nằm trong đơn hàng chưa hoàn tất.");
            }

            // map dto sang entity
            ComputerDevice deviceEntity = rehydrateEntity(deviceData);
            // entity chuyển trạng thái
            deviceEntity.softDelete();

            DeviceData dataToSave = mapCommonData(deviceEntity);
            mapSpecificDataToDTO(deviceEntity, dataToSave);

            deviceRepository.save(dataToSave);

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

    protected abstract ComputerDevice rehydrateEntity(DeviceData data);
    protected abstract void mapSpecificDataToDTO(ComputerDevice entity, DeviceData data);

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