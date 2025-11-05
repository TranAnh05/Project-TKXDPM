package domain.entities;

public class OrderDetail {
	private int id;
    private int orderId; // Khóa ngoại trỏ đến Order
    private int productId; // Khóa ngoại trỏ đến Product
    private int quantity; // Số lượng mua
    private double priceAtPurchase; // Giá tại thời điểm mua
    
    public OrderDetail(int id, int orderId, int productId, int quantity, double priceAtPurchase) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }
    
 // --- Getters ---
    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPriceAtPurchase() { return priceAtPurchase; }
}
