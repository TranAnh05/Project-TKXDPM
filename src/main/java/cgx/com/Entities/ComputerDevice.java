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
    private String categoryId; 
    private String status;     
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
    public static void validateCommonInfo(String name, String des, BigDecimal price, int stockQuantity) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        if (des == null || des.trim().isEmpty()) throw new IllegalArgumentException("Mô tả sản phẩm không được để trống.");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Giá bán không hợp lệ.");
        if (stockQuantity < 0) throw new IllegalArgumentException("Số lượng tồn kho không được âm.");
    }
    
    public void validateStock(int quantity) {
    	int currentStock = getStockQuantity();
    	if(currentStock < quantity) {
    		throw new IllegalArgumentException("Sản phẩm " + this.getName() + " không đủ hàng.");
    	}
    }
    
    public static void validateNewStatus(String status) {
    	if(status == null || status.trim().isEmpty()) {
    		throw new IllegalArgumentException("Trạng thái sản phẩm không được để trống.");
    	}
    	
    	try {
            ProductAvailability.valueOf(status); 
        } catch (IllegalArgumentException e) {
        	throw new IllegalArgumentException("Trạng thái sản phẩm không hợp lệ: " + status);
        }
    }
    
    public static void validateStatus(String status) {
    	if(ProductAvailability.valueOf(status) == ProductAvailability.DISCONTINUED) {
    		throw new IllegalArgumentException("Sản phẩm này hiện đang ngừng kinh doanh.");
    	}
    }
    
    public static void validateStockQuantity(int stockQuantity) {
    	if (stockQuantity <= 0) {
            throw new IllegalArgumentException("Sản phẩm đã hết hàng.");
        }	
    }
    
    
    public void minusStock(int quantity) {
    	this.stockQuantity -= quantity;
    	if (this.stockQuantity == 0 && !this.status.equals(ProductAvailability.DISCONTINUED.name())) {
            this.status = ProductAvailability.OUT_OF_STOCK.name();
        }
    }
    
    public void plusStock(int quantity) {
    	this.stockQuantity += quantity;
    }
    
    public void softDelete() {
        this.status = "DISCONTINUED";
        this.stockQuantity = 0;
        this.touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
    
    public ProductAvailability checkAvailability(int requestedQuantity) {
        if (this.stockQuantity == 0) {
            return ProductAvailability.OUT_OF_STOCK;
        }
        if (this.stockQuantity < requestedQuantity) {
            return ProductAvailability.NOT_ENOUGH_STOCK;
        }
        return ProductAvailability.AVAILABLE;
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