package cgx.com.adapters.ManageProduct.DeleteDevice;

import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteDeviceOutputBoundary;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteDeviceResponseData;

public class DeleteDevicePresenter implements DeleteDeviceOutputBoundary {

    private DeleteDeviceViewModel viewModel;

    public DeleteDevicePresenter(DeleteDeviceViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(DeleteDeviceResponseData responseData) {
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;
        viewModel.deletedId = responseData.deletedDeviceId;
        viewModel.status = responseData.newStatus;
    }
    
    public DeleteDeviceViewModel getModel() {
        return this.viewModel;
    }
}
