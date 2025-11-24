package cgx.com.adapters.ManageOrder.ViewOrderDetail;

import java.util.List;

public class OrderDetailViewDTO {
	public String orderId;
    public String totalAmount;
    public String status;
    public String shippingAddress;
    public String createdAt;
    public List<OrderItemViewDTO> items;
}
