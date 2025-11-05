package adapters.ManageOrder.UpdateOrderStatus;

import java.time.format.DateTimeFormatter;

import adapters.ManageOrder.OrderViewDTO;
import application.dtos.ManageOrder.OrderOutputData;
import application.dtos.ManageOrder.UpdateOrderStatus.UpdateOrderStatusOutputData;
import application.ports.out.ManageOrder.UpdateOrderStatus.UpdateOrderStatusOutputBoundary;

public class UpdateOrderStatusPresenter implements UpdateOrderStatusOutputBoundary{
	private UpdateOrderStatusViewModel viewModel;

	public UpdateOrderStatusPresenter(UpdateOrderStatusViewModel viewModel) { this.viewModel = viewModel; }

	public UpdateOrderStatusViewModel getViewModel() { return this.viewModel; }

	@Override
	public void present(UpdateOrderStatusOutputData output) {
		OrderViewDTO viewDTO = null;
        if (output.updatedOrder != null) {
            viewDTO = mapToViewDTO(output.updatedOrder);
        }
        viewModel.success = String.valueOf(output.success);
        viewModel.message = output.message;
        viewModel.updatedOrder = viewDTO;
		
	}

	private OrderViewDTO mapToViewDTO(OrderOutputData data) {
		OrderViewDTO dto = new OrderViewDTO();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");
		
        dto.id = String.valueOf(data.id);
        dto.userEmail = data.userEmail;
        dto.orderDate = data.orderDate.format(formatter); // LocalDateTime -> String
        dto.totalAmount = String.valueOf(data.totalAmount); // double -> String
        dto.status = data.status.name(); // Enum -> String
        
        return dto;
	}

}
