package cgx.com.usecase.Cart.UpdateCart;

public class UpdateCartRequestData {
	public String authToken;
    public String deviceId;
    public int newQuantity;

    public UpdateCartRequestData(String authToken, String deviceId, int newQuantity) {
        this.authToken = authToken;
        this.deviceId = deviceId;
        this.newQuantity = newQuantity;
    }
}
