package cgx.com.infrastructure.database.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("LAPTOP") // Giá trị lưu trong cột device_type
public class LaptopJpaEntity extends DeviceJpaEntity {

    private String cpu;
    private String ram;
    private String storage;
    private Double screenSize;

    public LaptopJpaEntity() {}

    // Getters & Setters
    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }
    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }
    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }
    public Double getScreenSize() { return screenSize; }
    public void setScreenSize(Double screenSize) { this.screenSize = screenSize; }
}