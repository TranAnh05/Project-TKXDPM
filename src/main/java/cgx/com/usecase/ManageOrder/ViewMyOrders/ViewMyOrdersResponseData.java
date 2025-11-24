package cgx.com.usecase.ManageOrder.ViewMyOrders;

import java.util.List;

import cgx.com.usecase.ManageOrder.OrderData;

public class ViewMyOrdersResponseData {
	public boolean success;
    public String message;
    // Trả về danh sách DTO Database, Presenter sẽ lo việc map sang View DTO
    public List<OrderData> orders;
}
