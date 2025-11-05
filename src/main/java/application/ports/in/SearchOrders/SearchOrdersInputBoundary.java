package application.ports.in.SearchOrders;

import application.dtos.SearchOrders.SearchOrdersInputData;

public interface SearchOrdersInputBoundary {
	void execute(SearchOrdersInputData input);
}
