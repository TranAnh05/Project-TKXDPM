package cgx.com.usecase.Cart.ViewCart;

import java.math.BigDecimal;

import cgx.com.Entities.ProductAvailability;

public class ViewCartItemData {
	public String deviceId;
    public String deviceName;
    public String thumbnail;
    public int quantity;
    public BigDecimal currentPrice; 
    public BigDecimal subTotal;     
    
    // Trả về Enum trạng thái, Presenter sẽ lo việc dịch ra tiếng Việt
    public ProductAvailability availabilityStatus; 
    public int currentStock; // Để hiển thị "Chỉ còn 2 cái"
}
