package cgx.com.Entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Order {
    private String id;
    private String userId; // Người mua
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private PaymentMethod paymentMethod;	
    private Instant createdAt;
    private Instant updatedAt;
    
    private List<OrderItem> items; // Danh sách sản phẩm

    public Order(String id, String userId, String shippingAddress, PaymentMethod paymentMethod) {
        this.id = id;
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.status = OrderStatus.PENDING; // Mặc định là chờ xử lý
        this.items = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public Order(String id, String userId, String shippingAddress, OrderStatus status, PaymentMethod paymentMethod, BigDecimal totalAmount) {
        this.id = id;
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.items = new ArrayList<>();
        this.totalAmount = totalAmount;
        this.updatedAt = Instant.now();
    }
    	
    public static void validateId(String id) {
    	if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID đơn hàng không được để trống.");
        }
    }
    
    
    public static void validateOrderInfo(String shippingAddress, String userId, Map<String, Integer> items, String paymentMethod) {
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ giao hàng không được để trống.");
        }
        
        if (userId == null || userId.trim().isEmpty()) {
             // UseCase đảm bảo userId có (từ token), nhưng Entity vẫn nên check để chắc chắn
             throw new IllegalArgumentException("Người dùng không hợp lệ.");
        }
        
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng không được để trống.");
        }
        
        if(paymentMethod == null) {
        	throw new IllegalArgumentException("Phương thức thanh toán không được để trống.");
        }
        
        try {
            // Cố gắng ép kiểu String sang Enum
            PaymentMethod.valueOf(paymentMethod); 
        } catch (IllegalArgumentException e) {
            // Nếu Java không tìm thấy Enum tương ứng, nó ném lỗi -> Ta bắt lại và báo lỗi nghiệp vụ
        	throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ: " + paymentMethod);
        }
    }
    
    // --- Domain Logic ---

    public void addItem(OrderItem item) {
        this.items.add(item);
        recalculateTotal();
        this.updatedAt = Instant.now();
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Không thể hủy đơn hàng đã được xử lý hoặc đang giao.");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void validateNewStatus(OrderStatus newStatus) {
    	if (this.status == newStatus) {
            throw new IllegalArgumentException("Đơn hàng đã ở trạng thái này rồi.");
        }
    }
    
    public void updateStatus(OrderStatus newStatus) {
        if (this.status == newStatus) {
            throw new IllegalArgumentException("Đơn hàng đã ở trạng thái này rồi.");
        }

        // Logic Hủy đơn
        if (newStatus == OrderStatus.CANCELLED) {
            if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
                throw new IllegalStateException("Không thể hủy đơn hàng đã được xử lý hoặc đang giao.");
            }
        } 
        // Logic các trạng thái khác (Không cho phép sửa đơn đã kết thúc)
        else {
            if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.DELIVERED) {
                throw new IllegalStateException("Đơn hàng đã kết thúc, không thể cập nhật trạng thái.");
            }
        }

        // Nếu hợp lệ, cập nhật trạng thái
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }
    
    // --- Getters ---
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public String getShippingAddress() { return shippingAddress; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
