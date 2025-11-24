package cgx.com.adapters.ManageOrder.ViewMyOrders;

import java.util.Collections;
import java.util.stream.Collectors;

import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.ViewMyOrders.ViewMyOrdersOutputBoundary;
import cgx.com.usecase.ManageOrder.ViewMyOrders.ViewMyOrdersResponseData;

public class ViewMyOrdersPresenter implements ViewMyOrdersOutputBoundary {

    private ViewMyOrdersViewModel viewModel;

    public ViewMyOrdersPresenter(ViewMyOrdersViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(ViewMyOrdersResponseData response) {
        viewModel.success = String.valueOf(response.success);
        viewModel.message = response.message;

        if (response.success && response.orders != null) {
            viewModel.orders = response.orders.stream()
                    .map(this::mapToViewDTO)
                    .collect(Collectors.toList());
        } else {
            viewModel.orders = Collections.emptyList();
        }
    }

    private OrderSummaryViewDTO mapToViewDTO(OrderData data) {
        OrderSummaryViewDTO dto = new OrderSummaryViewDTO();
        dto.orderId = data.id;
        // Format tiền tệ nên xử lý ở Frontend, Backend trả về số dạng String chuẩn
        dto.totalAmount = data.totalAmount != null ? data.totalAmount.toPlainString() : "0";
        dto.status = data.status;
        dto.createdAt = data.createdAt != null ? data.createdAt.toString() : "";
        dto.itemCount = data.items != null ? data.items.size() : 0;
        return dto;
    }

    public ViewMyOrdersViewModel getModel() {
        return viewModel;
    }
}