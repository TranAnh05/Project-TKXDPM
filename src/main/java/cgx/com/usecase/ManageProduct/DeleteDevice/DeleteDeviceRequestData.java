package cgx.com.usecase.ManageProduct.DeleteDevice;

public class DeleteDeviceRequestData {
	public final String authToken;
    public final String deviceId;

    public DeleteDeviceRequestData(String authToken, String deviceId) {
        this.authToken = authToken;
        this.deviceId = deviceId;
    }
}
