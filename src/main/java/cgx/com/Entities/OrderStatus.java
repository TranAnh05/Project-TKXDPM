package cgx.com.Entities;

public enum OrderStatus {
	PENDING,    // Chờ xác nhận
    CONFIRMED,  // Đã xác nhận
    SHIPPED,    // Đang giao
    DELIVERED,  // Đã giao
    CANCELLED   // Đã hủy
}
