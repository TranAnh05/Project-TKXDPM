package cgx.com.usecase.ManageProduct.DeleteDevice;

public class DeleteDeviceRequestData {
	public String authToken;
    public String deviceId;

    public DeleteDeviceRequestData(String authToken, String deviceId) {
        this.authToken = authToken;
        this.deviceId = deviceId;
    }

	public DeleteDeviceRequestData() {
		// TODO Auto-generated constructor stub
	}
}
