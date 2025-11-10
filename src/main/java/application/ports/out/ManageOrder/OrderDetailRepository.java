package application.ports.out.ManageOrder;

import java.util.List;

import application.dtos.ManageOrder.OrderDetailData;

public interface OrderDetailRepository {
	/**
     * Tìm tất cả Chi tiết đơn hàng (các sản phẩm)
     * thuộc về MỘT Đơn hàng (Order).
     */
    List<OrderDetailData> findAllByOrderId(int orderId);
}
