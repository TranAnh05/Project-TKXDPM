package cgx.com.usecase.ManageProduct.ViewDeviceDetail;

public class ViewDeviceDetailRequestData {
	public final String deviceId;
    public final String authToken; // Có thể null (nếu là Guest)

    public ViewDeviceDetailRequestData(String deviceId, String authToken) {
        this.deviceId = deviceId;
        this.authToken = authToken;
    }
}
