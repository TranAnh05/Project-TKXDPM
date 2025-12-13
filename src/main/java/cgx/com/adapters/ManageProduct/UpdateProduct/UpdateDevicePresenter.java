package cgx.com.adapters.ManageProduct.UpdateProduct;

import cgx.com.adapters.ManageProduct.DeviceViewDTO;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateDeviceOutputBoundary;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateDeviceResponseData;

public class UpdateDevicePresenter implements UpdateDeviceOutputBoundary {

    private UpdateDeviceViewModel viewModel;

    public UpdateDevicePresenter(UpdateDeviceViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(UpdateDeviceResponseData responseData) {
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        DeviceViewDTO viewDTO = null;
        if (responseData.success) {
        	viewModel.deviceId = responseData.deviceId;
        }
    }
    
    public UpdateDeviceViewModel getModel() {
        return this.viewModel;
    }
}
