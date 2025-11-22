package cgx.com.usecase.ManageProduct;


public interface IDeviceRepository {
	void save(DeviceData deviceData);
    boolean existsByName(String name);
    /**
     * Tìm thiết bị theo ID (MỚI).
     * @param id ID thiết bị
     * @return DeviceData chứa toàn bộ thông tin (Superset) hoặc null.
     */
    DeviceData findById(String id);
}
