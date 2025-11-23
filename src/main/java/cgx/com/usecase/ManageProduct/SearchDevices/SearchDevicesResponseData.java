package cgx.com.usecase.ManageProduct.SearchDevices;

import java.util.List;

import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageUser.SearchUsers.PaginationData;

public class SearchDevicesResponseData {
	public boolean success;
    public String message;
    public List<DeviceData> devices;
    // Tái sử dụng PaginationData từ nhóm User (vì nó là DTO logic chung)
    public PaginationData pagination;
}
