package cgx.com.usecase.ManageProduct.AdjustStock;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Laptop;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

/**
 * Use Case Cụ thể: Điều chỉnh tồn kho Laptop.
 */
public class AdjustLaptopStockUseCase extends AbstractAdjustStockUseCase {

    public AdjustLaptopStockUseCase(IDeviceRepository deviceRepository,
                                    IAuthTokenValidator tokenValidator,
                                    AdjustStockOutputBoundary outputBoundary) {
        super(deviceRepository, tokenValidator, outputBoundary);
    }

    @Override
    protected ComputerDevice rehydrateEntity(DeviceData data) {
        // Nếu dữ liệu không có CPU, không phải là Laptop
        if (data.cpu == null) return null;
        
        return new Laptop(
            data.id, data.name, data.description, data.price, data.stockQuantity,
            data.categoryId, data.status, data.thumbnail, data.createdAt, data.updatedAt,
            data.cpu, data.ram, data.storage, data.screenSize
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
