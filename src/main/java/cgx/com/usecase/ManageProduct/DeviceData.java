package cgx.com.usecase.ManageProduct;

import java.math.BigDecimal;
import java.time.Instant;

public class DeviceData {
    // Chung
    public String id;
    public String name;
    public String description;
    public BigDecimal price;
    public int stockQuantity;
    public String categoryId;
    public String status;
    public String thumbnail;
    public Instant createdAt;
    public Instant updatedAt;
    
    public String type;

    // Laptop specific
    public String cpu;
    public String ram;
    public String storage;
    public Double screenSize;

    // Mouse specific
    public Integer dpi;
    public Boolean isWireless;
    public Integer buttonCount;
    
    // Constructor mặc định
    public DeviceData() {}
}
