package application.ports.out.SearchOrders;

import application.dtos.SearchOrders.SearchOrdersOutputData;

public interface SearchOrdersOutputBoundary {
	void present(SearchOrdersOutputData output);
}
