package cgx.com.usecase.ManageProduct.SearchDevices;

import java.util.List;

import cgx.com.usecase.Interface_Common.PaginationData;
import cgx.com.usecase.ManageProduct.DeviceData;

public class SearchDevicesResponseData {
	public boolean success;
    public String message;
    public List<DeviceData> devices;
    public PaginationData pagination;
}
