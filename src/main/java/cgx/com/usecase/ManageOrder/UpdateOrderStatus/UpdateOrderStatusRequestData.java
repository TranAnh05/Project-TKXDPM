package cgx.com.usecase.ManageOrder.UpdateOrderStatus;

public class UpdateOrderStatusRequestData {
	public final String authToken;
    public final String orderId;
    public final String newStatus;

    public UpdateOrderStatusRequestData(String authToken, String orderId, String newStatus) {
        this.authToken = authToken;
        this.orderId = orderId;
        this.newStatus = newStatus;
    }
}
