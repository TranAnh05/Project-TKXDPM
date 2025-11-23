package cgx.com.Entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;
    private String userId; // Người mua
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private Instant createdAt;
    private Instant updatedAt;
    
    private List<OrderItem> items; // Danh sách sản phẩm

    public Order(String id, String userId, String shippingAddress) {
        this.id = id;
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PENDING; // Mặc định là chờ xử lý
        this.items = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    	
    private static void validateOrderInfo(String shippingAddress, String userId) {
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ giao hàng không được để trống.");
        }
        
        if (userId == null || userId.trim().isEmpty()) {
             // UseCase đảm bảo userId có (từ token), nhưng Entity vẫn nên check để chắc chắn
             throw new IllegalArgumentException("Người dùng không hợp lệ.");
        }
    }
    

    
    public void validateItems() {
        if (this.items == null || this.items.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng không được để trống.");
        }
    }
    
    // --- Domain Logic ---

    public void addItem(OrderItem item) {
        this.items.add(item);
        recalculateTotal();
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public String getShippingAddress() { return shippingAddress; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
