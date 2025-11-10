package application.dtos.ManageOrder;

public class OrderDetailData {
	public int id;
    public int orderId;
    public int productId;
    public int quantity;
    public double priceAtPurchase;
    
    // (Constructor để map từ CSDL)
    public OrderDetailData(int id, int orderId, int productId, int quantity, double priceAtPurchase) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }
}
