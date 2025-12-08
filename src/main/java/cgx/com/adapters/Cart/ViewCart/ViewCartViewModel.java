package cgx.com.adapters.Cart.ViewCart;

import java.util.ArrayList;
import java.util.List;

public class ViewCartViewModel {
	// Trạng thái request ("true"/"false")
    public String isSuccess; 
    
    // Thông báo lỗi hoặc thành công
    public String message;
    
    // Tổng tiền toàn giỏ hàng đã format (Ví dụ: "50.000.000 đ")
    public String totalCartPrice; 
    
    // Danh sách sản phẩm (đã map sang ViewModel con)
    public List<CartItemViewModel> items;

    public ViewCartViewModel() {
        this.items = new ArrayList<>();
    }
}
