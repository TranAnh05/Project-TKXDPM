package cgx.com.adapters.ManageOrder.ViewOrderDetail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageOrder.ViewOrderDetail.ViewOrderDetailOutputBoundary;
import cgx.com.usecase.ManageOrder.ViewOrderDetail.ViewOrderDetailResponseData;

public class ViewOrderDetailPresenter implements ViewOrderDetailOutputBoundary {

    private ViewOrderDetailViewModel viewModel;

    public ViewOrderDetailPresenter(ViewOrderDetailViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(ViewOrderDetailResponseData response) {
        viewModel.success = String.valueOf(response.success);
        viewModel.message = response.message;

        if (response.success && response.order != null) {
            viewModel.orderDetail = mapToViewDTO(response.order);
        } else {
            viewModel.orderDetail = null;
        }
    }

    private OrderDetailViewDTO mapToViewDTO(OrderData data) {
        OrderDetailViewDTO dto = new OrderDetailViewDTO();
        dto.orderId = data.id;
        dto.totalAmount = data.totalAmount != null ? data.totalAmount.toPlainString() : "0";
        dto.status = data.status;
        dto.shippingAddress = data.shippingAddress;
        dto.createdAt = data.createdAt != null ? data.createdAt.toString() : "";
        
        dto.items = new ArrayList<>();
        if (data.items != null) {
            for (OrderItemData itemData : data.items) {
                OrderItemViewDTO itemDto = new OrderItemViewDTO();
                itemDto.deviceId = itemData.deviceId;
                itemDto.deviceName = itemData.deviceName;
                itemDto.thumbnail = itemData.thumbnail;
                itemDto.unitPrice = itemData.unitPrice != null ? itemData.unitPrice.toPlainString() : "0";
                itemDto.quantity = String.valueOf(itemData.quantity);
                
                // Tính SubTotal để hiển thị (Price * Quantity)
                BigDecimal subTotal = (itemData.unitPrice != null) 
                    ? itemData.unitPrice.multiply(BigDecimal.valueOf(itemData.quantity)) 
                    : BigDecimal.ZERO;
                itemDto.subTotal = subTotal.toPlainString();
                
                dto.items.add(itemDto);
            }
        }
        return dto;
    }

    public ViewOrderDetailViewModel getModel() {
        return viewModel;
    }
}
