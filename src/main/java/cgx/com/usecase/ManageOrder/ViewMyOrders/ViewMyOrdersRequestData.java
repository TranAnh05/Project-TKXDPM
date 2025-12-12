package cgx.com.usecase.ManageOrder.ViewMyOrders;

public class ViewMyOrdersRequestData {
	public String authToken;

    public ViewMyOrdersRequestData(String authToken) {
        this.authToken = authToken;
    }

	public ViewMyOrdersRequestData() {
	}
}
