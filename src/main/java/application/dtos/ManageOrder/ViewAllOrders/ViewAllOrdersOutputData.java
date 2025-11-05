package application.dtos.ManageOrder.ViewAllOrders;

import java.util.List;

import application.dtos.ManageOrder.OrderOutputData;

public class ViewAllOrdersOutputData {
	public boolean success;
    public String message;
    public List<OrderOutputData> orders; 
}	
