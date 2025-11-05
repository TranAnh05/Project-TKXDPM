package application.ports.in.ManageOrder.UpdateOrderStatus;

import application.dtos.ManageOrder.UpdateOrderStatus.UpdateOrderStatusInputData;

public interface UpdateOrderStatusInputBoundary {
	void execute(UpdateOrderStatusInputData input);
}
