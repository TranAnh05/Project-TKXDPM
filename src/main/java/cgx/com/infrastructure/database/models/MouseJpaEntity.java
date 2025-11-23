package cgx.com.infrastructure.database.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MOUSE") // Giá trị lưu trong cột device_type
public class MouseJpaEntity extends DeviceJpaEntity {

    private Integer dpi;
    private Boolean isWireless;
    private Integer buttonCount;

    public MouseJpaEntity() {}

    // Getters & Setters
    public Integer getDpi() { return dpi; }
    public void setDpi(Integer dpi) { this.dpi = dpi; }
    public Boolean getIsWireless() { return isWireless; }
    public void setIsWireless(Boolean isWireless) { this.isWireless = isWireless; }
    public Integer getButtonCount() { return buttonCount; }
    public void setButtonCount(Integer buttonCount) { this.buttonCount = buttonCount; }
}