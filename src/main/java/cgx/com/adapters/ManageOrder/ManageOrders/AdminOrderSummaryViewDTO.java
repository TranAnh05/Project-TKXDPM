package cgx.com.adapters.ManageOrder.ManageOrders;

public class AdminOrderSummaryViewDTO {
	public String orderId;
    public String userId; // Thêm trường này so với Customer view
    public String totalAmount;
    public String status;
    public String createdAt;
    public int itemCount;
}
