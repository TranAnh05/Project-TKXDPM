package cgx.com.adapters.ManageOrder.ViewMyOrders;

public class OrderSummaryViewDTO {
	public String orderId;
    public String totalAmount;
    public String status;
    public String createdAt;
    public int itemCount; // Số lượng sản phẩm trong đơn (để hiển thị kiểu "3 items")
}
