package adapters.ManageOrder.ViewAllOrders;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import adapters.ManageOrder.OrderViewDTO;
import application.dtos.ManageOrder.OrderOutputData;
import application.dtos.ManageOrder.ViewAllOrders.ViewAllOrdersOutputData;
import application.ports.out.ManageOrder.ViewAllOrders.ViewAllOrdersOutputBoundary;

public class ViewAllOrdersPresenter implements ViewAllOrdersOutputBoundary{
	private ViewAllOrdersViewModel viewModel;

	public ViewAllOrdersPresenter(ViewAllOrdersViewModel viewModel) { this.viewModel = viewModel; }

	public ViewAllOrdersViewModel getViewModel() { return this.viewModel; }

	@Override
	public void present(ViewAllOrdersOutputData output) {
		List<OrderViewDTO> viewDTOs = new ArrayList<>();
        if (output.orders != null) {
            for (OrderOutputData orderData : output.orders) {
                viewDTOs.add(mapToViewDTO(orderData));
            }
        }
        
        viewModel.success = String.valueOf(output.success);
        viewModel.message = output.message;
        viewModel.orders = viewDTOs;
	}

	private OrderViewDTO mapToViewDTO(OrderOutputData data) {
		OrderViewDTO dto = new OrderViewDTO();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");
		DecimalFormat numberFormatter = new DecimalFormat("#");
		numberFormatter.setGroupingUsed(false);
		
        dto.id = String.valueOf(data.id);
        dto.userEmail = data.userEmail;
        dto.orderDate = data.orderDate.format(formatter); // LocalDateTime -> String
        dto.totalAmount = numberFormatter.format(data.totalAmount); // double -> String
        dto.status = data.status.name(); // Enum -> String
        return dto;
	}

}
