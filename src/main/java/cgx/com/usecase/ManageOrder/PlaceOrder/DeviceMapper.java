package cgx.com.usecase.ManageOrder.PlaceOrder;

import java.util.List;

import cgx.com.Entities.ComputerDevice;
import cgx.com.usecase.ManageProduct.DeviceData;

/**
 * Context của Strategy Pattern.
 * Hoàn toàn tuân thủ OCP. Không cần sửa file này khi thêm loại thiết bị mới.
 */
public class DeviceMapper implements IDeviceMapper {

    // Danh sách các chiến lược mapping (LaptopMapper, MouseMapper,...)
    private final List<IDeviceSpecificMapper> mappers;

    public DeviceMapper(List<IDeviceSpecificMapper> mappers) {
        this.mappers = mappers;
    }

    @Override
    public ComputerDevice toEntity(DeviceData data) {
        if (data == null) {
            throw new IllegalArgumentException("Dữ liệu sản phẩm không được để trống (null).");
        }
        
        // Kiểm tra type
        if (data.type == null || data.type.trim().isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu sản phẩm lỗi (thiếu thông tin định danh loại).");
        }

        // Tìm mapper phù hợp trong danh sách
        for (IDeviceSpecificMapper mapper : mappers) {
            if (mapper.supports(data.type)) {
                return mapper.toEntity(data);
            }
        }
        // Nếu không tìm thấy mapper nào hỗ trợ
        throw new IllegalArgumentException("Không hỗ trợ loại thiết bị: " + data.type);
    }

    @Override
    public DeviceData toDTO(ComputerDevice entity) {
        // Tương tự, nếu entity null -> ném lỗi
        if (entity == null) {
            throw new IllegalArgumentException("Entity sản phẩm không được để trống (null).");
        }
        
        // Xác định type từ class của entity (để tìm mapper)
        String type = entity.getClass().getSimpleName().toUpperCase(); // Ví dụ: LAPTOP, MOUSE

        for (IDeviceSpecificMapper mapper : mappers) {
            if (mapper.supports(type)) {
                DeviceData data = mapper.toDTO(entity);
                // Map các trường chung (Common fields) tại đây để tránh lặp lại ở các lớp con
                mapCommonFields(entity, data, type);
                return data;
            }
        }
        throw new IllegalArgumentException("Không hỗ trợ loại entity: " + type);
    }

    private void mapCommonFields(ComputerDevice entity, DeviceData data, String type) {
        data.id = entity.getId();
        data.name = entity.getName();
        data.description = "Desc"; // entity.getDescription();
        data.price = entity.getPrice();
        data.stockQuantity = entity.getStockQuantity();
        data.categoryId = entity.getCategoryId();
        data.status = entity.getStatus();
        data.thumbnail = entity.getThumbnail();
        data.createdAt = entity.getCreatedAt();
        data.updatedAt = entity.getUpdatedAt();
        data.type = type;
    }
}