package cgx.com.usecase.ManageProduct;

import java.util.List;

import cgx.com.usecase.ManageProduct.SearchDevices.DeviceSearchCriteria;

public interface IDeviceRepository {
	void save(DeviceData deviceData);
    boolean existsByName(String name);
    /**
     * Tìm thiết bị theo ID (MỚI).
     * @param id ID thiết bị
     * @return DeviceData chứa toàn bộ thông tin (Superset) hoặc null.
     */
    DeviceData findById(String id);
    /**
     * Tìm kiếm sản phẩm theo tiêu chí.
     * @param criteria Các tiêu chí lọc.
     * @param pageNumber Trang hiện tại (0-based).
     * @param pageSize Kích thước trang.
     * @return Danh sách kết quả.
     */
    List<DeviceData> search(DeviceSearchCriteria criteria, int pageNumber, int pageSize);

    /**
     * Đếm tổng số kết quả khớp với tiêu chí.
     */
    long count(DeviceSearchCriteria criteria);
}
