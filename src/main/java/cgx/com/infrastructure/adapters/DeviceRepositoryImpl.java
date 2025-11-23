package cgx.com.infrastructure.adapters;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import cgx.com.infrastructure.database.models.DeviceJpaEntity;
import cgx.com.infrastructure.database.models.LaptopJpaEntity;
import cgx.com.infrastructure.database.models.MouseJpaEntity;
import cgx.com.infrastructure.database.repositories.JpaDeviceRepository;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageProduct.SearchDevices.DeviceSearchCriteria;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeviceRepositoryImpl implements IDeviceRepository {

    private final JpaDeviceRepository jpaRepository;

    public DeviceRepositoryImpl(JpaDeviceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(DeviceData deviceData) {
        DeviceJpaEntity entity = mapToJpaEntity(deviceData);
        jpaRepository.save(entity);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public DeviceData findById(String id) {
        return jpaRepository.findById(id)
                .map(this::mapToDeviceData)
                .orElse(null);
    }

    @Override
    public List<DeviceData> search(DeviceSearchCriteria criteria, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return jpaRepository.searchDevices(
                criteria.keyword, criteria.categoryId, criteria.minPrice, criteria.maxPrice, criteria.status, pageable
        ).stream().map(this::mapToDeviceData).collect(Collectors.toList());
    }

    @Override
    public long count(DeviceSearchCriteria criteria) {
        return jpaRepository.countDevices(
                criteria.keyword, criteria.categoryId, criteria.minPrice, criteria.maxPrice, criteria.status
        );
    }

    // --- MAPPERS ---

    private DeviceData mapToDeviceData(DeviceJpaEntity entity) {
        DeviceData data = new DeviceData();
        // Chung
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

        // Riêng (Kiểm tra kiểu)
        if (entity instanceof LaptopJpaEntity) {
            LaptopJpaEntity l = (LaptopJpaEntity) entity;
            data.cpu = l.getCpu();
            data.ram = l.getRam();
            data.storage = l.getStorage();
            data.screenSize = l.getScreenSize();
        } else if (entity instanceof MouseJpaEntity) {
            MouseJpaEntity m = (MouseJpaEntity) entity;
            data.dpi = m.getDpi();
            data.isWireless = m.getIsWireless();
            data.buttonCount = m.getButtonCount();
        }
        return data;
    }

    private DeviceJpaEntity mapToJpaEntity(DeviceData data) {
        DeviceJpaEntity entity;

        // Quyết định tạo loại JPA Entity nào dựa trên dữ liệu trong DTO
        if (data.cpu != null) {
            LaptopJpaEntity l = new LaptopJpaEntity();
            l.setCpu(data.cpu);
            l.setRam(data.ram);
            l.setStorage(data.storage);
            l.setScreenSize(data.screenSize);
            entity = l;
        } else if (data.dpi != null) {
            MouseJpaEntity m = new MouseJpaEntity();
            m.setDpi(data.dpi);
            m.setIsWireless(data.isWireless);
            m.setButtonCount(data.buttonCount);
            entity = m;
        } else {
            // Fallback (không nên xảy ra nếu logic đúng)
            // Nhưng JPA Abstract class không thể new được. 
            // Ta có thể ném lỗi hoặc tạo 1 class Concrete Generic nếu cần.
            throw new IllegalArgumentException("Không xác định được loại thiết bị để lưu.");
        }

        // Map chung
        entity.setId(data.id);
        entity.setName(data.name);
        entity.setDescription(data.description);
        entity.setPrice(data.price);
        entity.setStockQuantity(data.stockQuantity);
        entity.setCategoryId(data.categoryId);
        entity.setStatus(data.status);
        entity.setThumbnail(data.thumbnail);
        entity.setCreatedAt(data.createdAt);
        entity.setUpdatedAt(data.updatedAt);

        return entity;
    }
}
