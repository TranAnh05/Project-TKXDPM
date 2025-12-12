package cgx.com.usecase.ManageOrder.CancelOrder;

public class CancelOrderRequestData {
	public String authToken;
    public String orderId;

    public CancelOrderRequestData(String authToken, String orderId) {
        this.authToken = authToken;
        this.orderId = orderId;
    }

	public CancelOrderRequestData() {
	}
}
