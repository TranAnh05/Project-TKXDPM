package cgx.com.Entities;

import java.math.BigDecimal;

public class OrderItem {
    private String deviceId;
    private String deviceName;
    private String thumbnail;
    private BigDecimal unitPrice; // Giá tại thời điểm mua
    private int quantity;

    public OrderItem(String deviceId, String deviceName, String thumbnail, BigDecimal unitPrice, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.thumbnail = thumbnail;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public BigDecimal getSubTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters
    public String getDeviceId() { return deviceId; }
    public String getDeviceName() { return deviceName; }
    public String getThumbnail() { return thumbnail; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
}
