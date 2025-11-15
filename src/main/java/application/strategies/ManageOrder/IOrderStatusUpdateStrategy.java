package application.strategies.ManageOrder;

import Entities.Order;

public interface IOrderStatusUpdateStrategy {
	/**
     * Thực thi logic nghiệp vụ (hệ quả)
     * KHI một đơn hàng được cập nhật.
     * @param order Entity (T4) của đơn hàng
     */
    void execute(Order order);
}
