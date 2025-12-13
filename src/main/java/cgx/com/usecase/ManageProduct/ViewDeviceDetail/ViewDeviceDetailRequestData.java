package cgx.com.usecase.ManageProduct.ViewDeviceDetail;

public class ViewDeviceDetailRequestData {
	public String deviceId;
    public String authToken; 

    public ViewDeviceDetailRequestData(String deviceId, String authToken) {
        this.deviceId = deviceId;
        this.authToken = authToken;
    }

	public ViewDeviceDetailRequestData() {
	}
}
