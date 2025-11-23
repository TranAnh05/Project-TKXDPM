package cgx.com.adapters.ManageProduct.SearchDevices;

import java.util.List;

import cgx.com.adapters.ManageUser.SearchUsers.PaginationViewDTO;

public class SearchDevicesViewModel {
	public String success;
    public String message;
    public List<DeviceSummaryViewDTO> devices;
    public PaginationViewDTO pagination; // Tái sử dụng từ nhóm User
}
