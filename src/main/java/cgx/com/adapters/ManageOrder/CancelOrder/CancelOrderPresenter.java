package cgx.com.adapters.ManageOrder.CancelOrder;

import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderOutputBoundary;
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderResponseData;

public class CancelOrderPresenter implements CancelOrderOutputBoundary {

    private CancelOrderViewModel viewModel;

    public CancelOrderPresenter(CancelOrderViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(CancelOrderResponseData response) {
        viewModel.success = String.valueOf(response.success);
        viewModel.message = response.message;
        if (response.success) {
            viewModel.orderId = response.orderId;
            viewModel.status = response.status;
        }
    }

    public CancelOrderViewModel getModel() {
        return viewModel;
    }
}