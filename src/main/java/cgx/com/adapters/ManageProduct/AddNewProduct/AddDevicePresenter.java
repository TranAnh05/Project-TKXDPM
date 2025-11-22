package cgx.com.adapters.ManageProduct.AddNewProduct;

import cgx.com.adapters.ManageProduct.DeviceViewDTO;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddDeviceOutputBoundary;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddDeviceResponseData;

public class AddDevicePresenter implements AddDeviceOutputBoundary {

    private AddDeviceViewModel viewModel;

    public AddDevicePresenter(AddDeviceViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(AddDeviceResponseData responseData) {
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        DeviceViewDTO viewDTO = null;
        if (responseData.success) {
            viewDTO = new DeviceViewDTO();
            viewDTO.id = responseData.newDeviceId;
            // Logic lấy tên có thể thêm vào ResponseData nếu cần,
            // hiện tại response chỉ có ID.
            viewDTO.name = ""; 
        }
        viewModel.newDevice = viewDTO;
    }
    
    public AddDeviceViewModel getModel() {
        return this.viewModel;
    }
}
