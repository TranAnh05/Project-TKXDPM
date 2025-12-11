package cgx.com.Entities;

public class CartItem {
    private String deviceId;
    private int quantity;

    public CartItem(String deviceId, int quantity) {
        this.deviceId = deviceId;
        this.quantity = quantity;
    }

    public static void validateQuantity(int quantity) {
    	if (quantity <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
    }
    
    public void addQuantity(int extraQuantity) {
        this.quantity += extraQuantity;
    }
    
    // Setter để update số lượng cụ thể (dùng cho tính năng Edit Cart sau này)
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDeviceId() { return deviceId; }
    public int getQuantity() { return quantity; }
}