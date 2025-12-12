package cgx.com.usecase.ManageOrder.PlaceOrder;

import java.util.Map;

public class PlaceOrderRequestData {
	public String authToken;
    public String shippingAddress;
    // Map<DeviceId, Quantity>: Ví dụ {"mouse-1": 2, "laptop-1": 1}
    // Đây là danh sách sản phẩm trong giỏ hàng mà user muốn mua
    public Map<String, Integer> cartItems; 
    public String paymentMethod = "COD";

    public PlaceOrderRequestData(String authToken, String shippingAddress, Map<String, Integer> cartItems) {
        this.authToken = authToken;
        this.shippingAddress = shippingAddress;
        this.cartItems = cartItems;
    }
}
