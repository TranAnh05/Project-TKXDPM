package cgx.com.usecase.Cart.RemoveFromCart;

public class RemoveFromCartRequestData {
	public final String authToken;
    public final String deviceId;

    public RemoveFromCartRequestData(String authToken, String deviceId) {
        this.authToken = authToken;
        this.deviceId = deviceId;
    }
}
