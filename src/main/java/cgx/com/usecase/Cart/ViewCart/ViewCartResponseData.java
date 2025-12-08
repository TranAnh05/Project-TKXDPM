package cgx.com.usecase.Cart.ViewCart;

import java.math.BigDecimal;
import java.util.List;

public class ViewCartResponseData {
	public boolean success;
    public String message;
    public List<ViewCartItemData> items;
    public BigDecimal totalCartPrice; // Tổng tiền dự tính của cả giỏ
}
