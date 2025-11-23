package cgx.com.adapters.ManageProduct.ViewDeviceDetail;

import java.math.BigDecimal;

import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailOutputBoundary;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailResponseData;

public class ViewDeviceDetailPresenter implements ViewDeviceDetailOutputBoundary {

    private ViewDeviceDetailViewModel viewModel;

    public ViewDeviceDetailPresenter(ViewDeviceDetailViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(ViewDeviceDetailResponseData responseData) {
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        DeviceDetailViewDTO viewDTO = null;
        if (responseData.success && responseData.device != null) {
            viewDTO = mapToViewDTO(responseData.device);
        }
        viewModel.device = viewDTO;
    }

    private DeviceDetailViewDTO mapToViewDTO(DeviceData data) {
        DeviceDetailViewDTO dto = new DeviceDetailViewDTO();
        // Map chung
        dto.id = data.id;
        dto.name = data.name;
        dto.description = data.description;
        dto.price = data.price != null ? data.price.toPlainString() : "0";
        dto.stockQuantity = String.valueOf(data.stockQuantity);
        dto.categoryId = data.categoryId;
        dto.status = data.status;
        dto.thumbnail = data.thumbnail;
        dto.updatedAt = data.updatedAt != null ? data.updatedAt.toString() : "";

        // Map Laptop
        dto.cpu = data.cpu;
        dto.ram = data.ram;
        dto.storage = data.storage;
        dto.screenSize = data.screenSize != null ? String.valueOf(data.screenSize) : null;

        // Map Mouse
        dto.dpi = data.dpi != null ? String.valueOf(data.dpi) : null;
        dto.isWireless = data.isWireless != null ? String.valueOf(data.isWireless) : null;
        dto.buttonCount = data.buttonCount != null ? String.valueOf(data.buttonCount) : null;

        return dto;
    }

    public ViewDeviceDetailViewModel getModel() {
        return this.viewModel;
    }
}