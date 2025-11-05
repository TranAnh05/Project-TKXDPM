package application.dtos.SearchOrders;

import java.util.List;

import application.dtos.ManageOrder.OrderOutputData;

public class SearchOrdersOutputData {
	public boolean success;
    public String message;
    public List<OrderOutputData> orders;
}
