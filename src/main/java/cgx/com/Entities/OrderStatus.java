package cgx.com.Entities;

public enum OrderStatus {
	PENDING,    // Mới đặt, chờ xử lý
    PROCESSING, // Đang xử lý (đóng gói)
    SHIPPED,    // Đã giao cho vận chuyển
    DELIVERED,  // Đã giao thành công
    CANCELLED   // Đã hủy
}
