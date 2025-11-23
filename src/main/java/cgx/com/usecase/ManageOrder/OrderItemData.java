package cgx.com.usecase.ManageOrder;

import java.math.BigDecimal;

public class OrderItemData {
	public String deviceId;
    public String deviceName;
    public String thumbnail;
    public BigDecimal unitPrice;
    public int quantity;
    
    public OrderItemData() {}
    
    public OrderItemData(String deviceId, String deviceName, String thumbnail, BigDecimal unitPrice, int quantity) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.thumbnail = thumbnail;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
}
