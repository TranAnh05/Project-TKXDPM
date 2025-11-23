package cgx.com.usecase.ManageOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderData {
	public String id;
    public String userId;
    public BigDecimal totalAmount;
    public String status; // Enum -> String
    public String shippingAddress;
    public Instant createdAt;
    public Instant updatedAt;
    
    public List<OrderItemData> items; // Danh sách chi tiết
    
    public OrderData() {}
}
