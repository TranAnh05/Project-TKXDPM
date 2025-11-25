package cgx.com.adapters.ManageOrder.ManageOrders;

import java.util.List;

import cgx.com.adapters.ManageUser.SearchUsers.PaginationViewDTO;

public class ManageOrdersViewModel {
	public String success;
    public String message;
    public List<AdminOrderSummaryViewDTO> orders;
    public PaginationViewDTO pagination;
}
