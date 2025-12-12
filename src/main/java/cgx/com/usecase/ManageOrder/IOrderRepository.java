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
	
	 /**
     * Tìm đơn hàng theo ID.
     * @param orderId ID đơn hàng.
     * @return OrderData hoặc null.
     */
    OrderData findById(String orderId);
    
    /**
     * Tìm kiếm đơn hàng theo tiêu chí (Phân trang).
     */
    List<OrderData> search(OrderSearchCriteria criteria, int pageNumber, int pageSize);

    /**
     * Đếm tổng số đơn hàng theo tiêu chí.
     */
    long count(OrderSearchCriteria criteria);
}
