package cgx.com.Entities;

import java.time.LocalDateTime;

public class Order {
	private int id;
    private int userId; // ID của Customer đặt hàng
    private LocalDateTime orderDate; // Ngày giờ đặt hàng
    private double totalAmount; // Tổng giá trị đơn hàng
    private OrderStatus status; // Trạng thái (dùng Enum)
    
    public Order(int userId, LocalDateTime orderDate, double totalAmount, OrderStatus status) {
        // (Validation có thể thêm ở đây, ví dụ: totalAmount > 0)
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }
    
    public Order(int id, int userId, LocalDateTime orderDate, double totalAmount, OrderStatus status) {
        this(userId, orderDate, totalAmount, status);
        this.id = id;
    }
    
    // --- Getters ---
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    
    // --- Logic nghiệp vụ (Update) ---
    public void setStatus(OrderStatus status) {
        // (Sau này có thể thêm logic, ví dụ: không thể đổi từ SHIPPED về PENDING)
        this.status = status;
    }
    
    public static void validateStatus(String statusName) {
        if (statusName == null || statusName.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái không được rỗng.");
        }
        else {
        	boolean hasStatus = false;
        	for(OrderStatus status : OrderStatus.values()) {
        		if(statusName.equalsIgnoreCase(status.name())) {
        			hasStatus = true;
        		}
        	}
        	
        	if(!hasStatus) {
        		throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        	}
        }
    }
}

