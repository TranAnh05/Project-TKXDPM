package cgx.com.Entities;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Lớp Cha Trừu tượng: Thiết bị Máy tính.
 * Chứa các thuộc tính chung cho mọi sản phẩm.
 */
public abstract class ComputerDevice {
    private String id;
    private String name;
    private String description = "";
    private BigDecimal price;
    private int stockQuantity;
    private String categoryId; // Liên kết với nhóm Product Type
    private String status;     // "ACTIVE", "OUT_OF_STOCK"
    private String thumbnail;
    private Instant createdAt;
    private Instant updatedAt;

    public ComputerDevice(String id, String name, String description, BigDecimal price, 
                          int stockQuantity, String categoryId, String status, String thumbnail,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.categoryId = categoryId;
        this.status = status;
        this.thumbnail = thumbnail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- Validate Id
    public static void validateId(String id) {
    	if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID sản phẩm không được để trống.");
    }
    
    // --- Validation Chung ---
    public static void validateCommonInfo(String name, BigDecimal price, int stockQuantity) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Giá bán không hợp lệ.");
        if (stockQuantity < 0) throw new IllegalArgumentException("Số lượng tồn kho không được âm.");
    }
    
    public void validateStock(int quantity) {
    	int currentStock = getStockQuantity();
    	if(currentStock < quantity) {
    		throw new IllegalArgumentException("Sản phẩm " + this.getName() + " không đủ hàng.");
    	}
    }
    
    // --- BUSINESS LOGIC MỚI: Cập nhật tồn kho ---
    /**
     * Cập nhật số lượng tồn kho.
     * Phương thức này đảm bảo quy tắc nghiệp vụ luôn được tuân thủ.
     */
    public void updateStock(int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm.");
        }
        this.stockQuantity = newQuantity;
        
        // Logic nghiệp vụ: Nếu hết hàng thì chuyển trạng thái, nếu có hàng thì Active
        if (this.stockQuantity == 0) {
            this.status = "OUT_OF_STOCK";
        } else if ("OUT_OF_STOCK".equals(this.status)) {
            this.status = "ACTIVE";
        }
        
        this.touch(); // Cập nhật thời gian
    }
    
    public void minusStock(int quantity) {
    	this.stockQuantity -= quantity;
    }
    
    // --- LOGIC MỚI: Xóa mềm ---
    public void softDelete() {
        // Chuyển trạng thái sang DELETED (hoặc INACTIVE tùy quy ước)
        this.status = "DELETED";
        this.touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description;}
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public String getCategoryId() { return categoryId; }
    public String getStatus() { return status; }
    public String getThumbnail() { return thumbnail; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}