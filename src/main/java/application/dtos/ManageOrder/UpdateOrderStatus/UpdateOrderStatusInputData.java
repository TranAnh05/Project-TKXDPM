package application.dtos.ManageOrder.UpdateOrderStatus;

public class UpdateOrderStatusInputData {
	public int orderId;
    public String newStatus;
    
	public UpdateOrderStatusInputData(int orderId, String newStatus) {
		this.orderId = orderId;
		this.newStatus = newStatus;
	}
}
