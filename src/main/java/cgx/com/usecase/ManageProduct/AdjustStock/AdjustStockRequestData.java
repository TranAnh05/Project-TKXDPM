package cgx.com.usecase.ManageProduct.AdjustStock;

public class AdjustStockRequestData {
	public final String authToken;
    public final String deviceId;
    public final int newQuantity; // Số lượng tồn kho mới (SET)

    public AdjustStockRequestData(String authToken, String deviceId, int newQuantity) {
        this.authToken = authToken;
        this.deviceId = deviceId;
        this.newQuantity = newQuantity;
    }
}
