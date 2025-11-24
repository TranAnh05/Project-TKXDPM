package cgx.com.usecase.ManageOrder.CancelOrder;

public class CancelOrderRequestData {
	public final String authToken;
    public final String orderId;

    public CancelOrderRequestData(String authToken, String orderId) {
        this.authToken = authToken;
        this.orderId = orderId;
    }
}
