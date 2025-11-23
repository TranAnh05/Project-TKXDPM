package cgx.com.adapters.ManageOrder.PlaceOrder;

import cgx.com.usecase.ManageOrder.PlaceOrder.PlaceOrderOutputBoundary;
import cgx.com.usecase.ManageOrder.PlaceOrder.PlaceOrderResponseData;

public class PlaceOrderPresenter implements PlaceOrderOutputBoundary {

    private PlaceOrderViewModel viewModel;

    public PlaceOrderPresenter(PlaceOrderViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(PlaceOrderResponseData response) {
    	viewModel.success = String.valueOf(response.success);
        viewModel.message = response.message;
        
        OrderViewDTO viewDTO = null;
        if (response.success) {
            viewDTO = new OrderViewDTO();
            viewDTO.orderId = response.orderId;
            viewDTO.totalAmount = response.totalAmount.toPlainString();
        }
        viewModel.order = viewDTO;
    }

    public PlaceOrderViewModel getModel() {
        return viewModel;
    }
}