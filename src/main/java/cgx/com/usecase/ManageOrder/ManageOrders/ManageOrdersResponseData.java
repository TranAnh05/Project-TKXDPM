package cgx.com.usecase.ManageOrder.ManageOrders;

import java.util.List;

import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageUser.SearchUsers.PaginationData;

public class ManageOrdersResponseData {
	public boolean success;
    public String message;
    public List<OrderData> orders;
    public PaginationData pagination;
}
