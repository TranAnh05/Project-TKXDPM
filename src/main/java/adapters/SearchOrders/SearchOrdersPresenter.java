package adapters.SearchOrders;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import adapters.ManageOrder.OrderViewDTO;
import application.dtos.ManageOrder.OrderOutputData;
import application.dtos.SearchOrders.SearchOrdersOutputData;
import application.ports.out.SearchOrders.SearchOrdersOutputBoundary;

public class SearchOrdersPresenter implements SearchOrdersOutputBoundary{
	private SearchOrdersViewModel viewModel;
    
    public SearchOrdersPresenter(SearchOrdersViewModel viewModel) { this.viewModel = viewModel; }
	
    public SearchOrdersViewModel getViewModel() { return this.viewModel; }
    
	@Override
	public void present(SearchOrdersOutputData output) {
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
		
        dto.id = String.valueOf(data.id);
        dto.userEmail = data.userEmail;
        dto.orderDate = data.orderDate.format(formatter);
        dto.totalAmount = String.valueOf(data.totalAmount);
        dto.status = data.status.name();
        
        return dto;
	}

}
