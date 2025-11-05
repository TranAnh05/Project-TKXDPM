package Order;

import java.util.HashSet;
import java.util.Set;

import application.ports.out.ManageOrder.OrderRepository;

public class FakeOrderRepository implements OrderRepository{
	private Set<Integer> productsInUse = new HashSet<>();
	
	@Override
	public boolean isProductInAnyOrder(int productId) {
		return productsInUse.contains(productId);
	}
	
	// === Hàm helper cho Test ===
    public void setProductInUse(int productId) {
        productsInUse.add(productId);
    }

}
