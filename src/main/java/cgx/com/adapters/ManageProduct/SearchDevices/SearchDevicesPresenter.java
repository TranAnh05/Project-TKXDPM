package cgx.com.adapters.ManageProduct.SearchDevices;

import java.util.Collections;
import java.util.stream.Collectors;

import cgx.com.adapters.ManageUser.SearchUsers.PaginationViewDTO;
import cgx.com.usecase.Interface_Common.PaginationData;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.SearchDevices.SearchDevicesOutputBoundary;
import cgx.com.usecase.ManageProduct.SearchDevices.SearchDevicesResponseData;

public class SearchDevicesPresenter implements SearchDevicesOutputBoundary {

    private SearchDevicesViewModel viewModel;

    public SearchDevicesPresenter(SearchDevicesViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(SearchDevicesResponseData responseData) {
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        if (responseData.success) {
            viewModel.devices = responseData.devices.stream()
                .map(this::mapToViewDTO)
                .collect(Collectors.toList());
            
            viewModel.pagination = mapPaginationToViewDTO(responseData.pagination);
        } else {
            viewModel.devices = Collections.emptyList();
            viewModel.pagination = null;
        }
    }

    private DeviceSummaryViewDTO mapToViewDTO(DeviceData data) {
        DeviceSummaryViewDTO dto = new DeviceSummaryViewDTO();
        dto.id = data.id;
        dto.name = data.name;
        dto.price = data.price != null ? data.price.toPlainString() : "0";
        dto.thumbnail = data.thumbnail;
        dto.status = data.status;
        dto.categoryId = data.categoryId;
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

    public SearchDevicesViewModel getModel() {
        return this.viewModel;
    }
}