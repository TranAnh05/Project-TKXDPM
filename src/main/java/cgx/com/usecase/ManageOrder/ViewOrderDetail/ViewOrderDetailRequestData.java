package cgx.com.usecase.ManageOrder.ViewOrderDetail;

public class ViewOrderDetailRequestData {
	public final String authToken;
    public final String orderId;

    public ViewOrderDetailRequestData(String authToken, String orderId) {
        this.authToken = authToken;
        this.orderId = orderId;
    }
}
