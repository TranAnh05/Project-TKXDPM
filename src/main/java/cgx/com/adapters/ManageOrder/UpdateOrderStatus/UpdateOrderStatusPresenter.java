package cgx.com.adapters.ManageOrder.UpdateOrderStatus;

import cgx.com.usecase.ManageOrder.UpdateOrderStatus.UpdateOrderStatusOutputBoundary;
import cgx.com.usecase.ManageOrder.UpdateOrderStatus.UpdateOrderStatusResponseData;

public class UpdateOrderStatusPresenter implements UpdateOrderStatusOutputBoundary {

    private UpdateOrderStatusViewModel viewModel;

    public UpdateOrderStatusPresenter(UpdateOrderStatusViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(UpdateOrderStatusResponseData response) {
        viewModel.success = String.valueOf(response.success);
        viewModel.message = response.message;
        if (response.success) {
            viewModel.orderId = response.orderId;
            viewModel.status = response.status;
        }
    }

    public UpdateOrderStatusViewModel getModel() {
        return viewModel;
    }
}
