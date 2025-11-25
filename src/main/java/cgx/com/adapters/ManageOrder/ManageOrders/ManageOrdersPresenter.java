package cgx.com.adapters.ManageOrder.ManageOrders;

import java.util.Collections;
import java.util.stream.Collectors;

import cgx.com.adapters.ManageUser.SearchUsers.PaginationViewDTO;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.ManageOrders.ManageOrdersOutputBoundary;
import cgx.com.usecase.ManageOrder.ManageOrders.ManageOrdersResponseData;
import cgx.com.usecase.ManageUser.SearchUsers.PaginationData;

public class ManageOrdersPresenter implements ManageOrdersOutputBoundary {

    private ManageOrdersViewModel viewModel;

    public ManageOrdersPresenter(ManageOrdersViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(ManageOrdersResponseData response) {
        viewModel.success = String.valueOf(response.success);
        viewModel.message = response.message;

        if (response.success) {
            if (response.orders != null) {
                viewModel.orders = response.orders.stream()
                    .map(this::mapToViewDTO)
                    .collect(Collectors.toList());
            } else {
                viewModel.orders = Collections.emptyList();
            }
            
            if (response.pagination != null) {
                viewModel.pagination = mapPaginationToViewDTO(response.pagination);
            }
        } else {
            viewModel.orders = Collections.emptyList();
            viewModel.pagination = null;
        }
    }

    private AdminOrderSummaryViewDTO mapToViewDTO(OrderData data) {
        AdminOrderSummaryViewDTO dto = new AdminOrderSummaryViewDTO();
        dto.orderId = data.id;
        dto.userId = data.userId; // Admin cần biết ai mua
        dto.totalAmount = data.totalAmount != null ? data.totalAmount.toPlainString() : "0";
        dto.status = data.status;
        dto.createdAt = data.createdAt != null ? data.createdAt.toString() : "";
        dto.itemCount = data.items != null ? data.items.size() : 0;
        return dto;
    }

    private PaginationViewDTO mapPaginationToViewDTO(PaginationData data) {
        PaginationViewDTO dto = new PaginationViewDTO();
        dto.totalCount = String.valueOf(data.totalCount);
        dto.currentPage = String.valueOf(data.currentPage);
        dto.pageSize = String.valueOf(data.pageSize);
        dto.totalPages = String.valueOf(data.totalPages);
        return dto;
    }

    public ManageOrdersViewModel getModel() {
        return viewModel;
    }
}