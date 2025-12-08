package cgx.com.usecase.Cart;

public class CartItemData {
	public String deviceId;
    public int quantity;
    
    public CartItemData() {}
    public CartItemData(String deviceId, int quantity) {
        this.deviceId = deviceId;
        this.quantity = quantity;
    }
}
