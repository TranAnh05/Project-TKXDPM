package cgx.com.usecase.ManageOrder;

import java.util.List;

public interface IOrderRepository {
	void save(OrderData orderData);
	
	/**
     * Tìm danh sách đơn hàng của một User.
     * @param userId ID của người dùng.
     * @return Danh sách OrderData (sắp xếp giảm dần theo ngày tạo).
     */
	List<OrderData> findByUserId(String userId);
}
