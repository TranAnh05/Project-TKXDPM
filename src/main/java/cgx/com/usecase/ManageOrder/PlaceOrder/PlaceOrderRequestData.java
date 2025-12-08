package cgx.com.usecase.ManageOrder.PlaceOrder;

import java.util.Map;

public class PlaceOrderRequestData {
	public final String authToken;
    public final String shippingAddress;
    // Map<DeviceId, Quantity>: Ví dụ {"mouse-1": 2, "laptop-1": 1}
    // Đây là danh sách sản phẩm trong giỏ hàng mà user muốn mua
    public final Map<String, Integer> cartItems; 
    public final String paymentMethod; 

    public PlaceOrderRequestData(String authToken, String shippingAddress, Map<String, Integer> cartItems, String paymentMethod) {
        this.authToken = authToken;
        this.shippingAddress = shippingAddress;
        this.cartItems = cartItems;
        this.paymentMethod = paymentMethod;
    }
}
