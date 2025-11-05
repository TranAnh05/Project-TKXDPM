package application.ports.out.ManageOrder.UpdateOrderStatus;

import application.dtos.ManageOrder.UpdateOrderStatus.UpdateOrderStatusOutputData;

public interface UpdateOrderStatusOutputBoundary {
	void present(UpdateOrderStatusOutputData output);
}
