package cgx.com.usecase.ManageOrder.ManageOrders;

public class ManageOrdersRequestData {
	public final String authToken;
    public final String statusFilter; // Optional
    public final String userIdFilter; // Optional
    public final int page;
    public final int size;

    public ManageOrdersRequestData(String authToken, String statusFilter, String userIdFilter, int page, int size) {
        this.authToken = authToken;
        this.statusFilter = statusFilter;
        this.userIdFilter = userIdFilter;
        this.page = (page <= 0) ? 1 : page;
        this.size = (size <= 0) ? 10 : size;
    }
}
