package cgx.com.usecase.Cart.AddToCart;

public class AddToCartRequestData {
	public String authToken;
    public String deviceId;
    public int quantity;

    public AddToCartRequestData(String authToken, String deviceId, int quantity) {
        this.authToken = authToken;
        this.deviceId = deviceId;
        this.quantity = quantity;
    }
}
