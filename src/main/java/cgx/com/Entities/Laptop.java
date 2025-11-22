package cgx.com.Entities;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Lớp Con: Laptop.
 * Có các thuộc tính riêng: RAM, CPU, Screen Size...
 */
public class Laptop extends ComputerDevice {
    private String cpu;
    private String ram; // Ví dụ: "16GB DDR5"
    private String storage; // Ví dụ: "512GB SSD"
    private double screenSize; // Ví dụ: 14.0

    public Laptop(String id, String name, String description, BigDecimal price, int stockQuantity, 
                  String categoryId, String status, String thumbnail, Instant createdAt, Instant updatedAt,
                  String cpu, String ram, String storage, double screenSize) {
        super(id, name, description, price, stockQuantity, categoryId, status, thumbnail, createdAt, updatedAt);
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.screenSize = screenSize;
    }

    // --- Validation Riêng ---
    public static void validateSpecs(String cpu, String ram, String storage, double screenSize) {
        if (cpu == null || cpu.isEmpty()) throw new IllegalArgumentException("Thông tin CPU không được trống.");
        if (ram == null || ram.isEmpty()) throw new IllegalArgumentException("Thông tin RAM không được trống.");
        if(storage == null || storage.isEmpty()) throw new IllegalArgumentException("Thông tin Storage không được trống.");
        if (screenSize <= 0) throw new IllegalArgumentException("Kích thước màn hình không hợp lệ.");
    }
    
    // Getters riêng...
    public String getCpu() { return cpu; }
    public String getRam() { return ram; }
    public String getStorage() { return storage; }
    public double getScreenSize() { return screenSize; }
}