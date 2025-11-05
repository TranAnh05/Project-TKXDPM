package application.ports.out.ManageOrder.ViewAllOrders;

import application.dtos.ManageOrder.ViewAllOrders.ViewAllOrdersOutputData;

public interface ViewAllOrdersOutputBoundary {
	void present(ViewAllOrdersOutputData output);
}
