package cgx.com.usecase.ManageOrder.ViewOrderDetail;

public class ViewOrderDetailRequestData {
	public String authToken;
    public String orderId;

    public ViewOrderDetailRequestData(String authToken, String orderId) {
        this.authToken = authToken;
        this.orderId = orderId;
    }

	public ViewOrderDetailRequestData() {
	}
}
