package cgx.com.usecase.Cart;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CartData {
	public String userId;
    public List<CartItemData> items = new ArrayList<>();
    public BigDecimal totalEstimatedPrice; // Lưu tổng tiền tạm tính
    public Instant updatedAt;
    
    public CartData() {
        this.totalEstimatedPrice = BigDecimal.ZERO;
    }
}
