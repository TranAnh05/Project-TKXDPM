package cgx.com.infrastructure.database.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity JPA Cha.
 * Sử dụng chiến lược SINGLE_TABLE: Tất cả con cái lưu chung 1 bảng 'devices'.
 * Cột 'device_type' sẽ phân biệt loại (LAPTOP, MOUSE).
 */
@Entity
@Table(name = "devices")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "device_type", discriminatorType = DiscriminatorType.STRING)
public abstract class DeviceJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "stock_quantity")
    private int stockQuantity;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "status")
    private String status;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
    
    // Insertable=false, Updatable=false để chỉ đọc cột phân loại
    @Column(name = "device_type", insertable = false, updatable = false)
    private String deviceType;

    public DeviceJpaEntity() {}

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getDeviceType() { return deviceType; }
}