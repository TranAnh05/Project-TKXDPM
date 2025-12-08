package cgx.com.adapters.Cart.ViewCart;

public class CartItemViewModel {
	public String deviceId;
    public String deviceName;
    public String thumbnail;
    
    // Số lượng (String để hiển thị text input)
    public String quantity; 
    
    // Giá tiền đã format (Ví dụ: "20.000.000 đ")
    public String unitPrice; 
    public String subTotal;
    
    // Trạng thái hiển thị (Ví dụ: "Tạm hết hàng", "Còn hàng")
    public String statusLabel; 
    public String statusColor; // Gợi ý màu sắc cho UI (Ví dụ: "RED", "GREEN")
    
    // Cờ cho phép chọn mua/checkbox ("true"/"false")
    public String isBuyable; 

    public CartItemViewModel() {}        // true/false (để UI disable checkbox mua)
}
