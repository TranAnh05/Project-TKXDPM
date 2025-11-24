package cgx.com.usecase.ManageOrder.CancelOrder;

public class CancelOrderResponseData {
	public boolean success;
    public String message;
    public String orderId;
    public String status; // Trạng thái mới (CANCELLED)
}
