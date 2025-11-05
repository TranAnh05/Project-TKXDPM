package Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import application.dtos.ManageOrder.OrderData;
import application.ports.out.ManageOrder.OrderRepository;

public class FakeOrderRepository implements OrderRepository{
	private Set<Integer> productsInUse = new HashSet<>();
	private Map<Integer, OrderData> orderDatabase = new HashMap<>();
	private int sequence = 0;
	
	@Override
	public boolean isProductInAnyOrder(int productId) {
		return productsInUse.contains(productId);
	}
	
	// === Hàm helper cho Test ===
    public void setProductInUse(int productId) {
        productsInUse.add(productId);
    }

	@Override
	public OrderData findById(int id) {
		return orderDatabase.get(id);
	}

	@Override
	public List<OrderData> findAll() {
		return new ArrayList<>(orderDatabase.values());
	}

	@Override
	public OrderData update(OrderData orderData) {
		if (orderDatabase.containsKey(orderData.id)) {
			OrderData updatedData = new OrderData(
					orderData.id, orderData.userId, orderData.orderDate,
					orderData.totalAmount, orderData.status
            );
			
            orderDatabase.put(orderData.id, updatedData);
            return updatedData;
        }
        return null;
	}

	@Override
	public OrderData save(OrderData orderData) {
		sequence++;
        OrderData savedData = new OrderData(
            sequence, orderData.userId, orderData.orderDate,
            orderData.totalAmount, orderData.status
        );
        orderDatabase.put(sequence, savedData);
        return savedData;
	}

	@Override
	public List<OrderData> findAllByUserIds(List<Integer> userIds) {
		List<OrderData> results = new ArrayList<>();
        // (Đây là cách đơn giản để convert List -> Set để tra cứu nhanh O(1))
        Set<Integer> idSet = new HashSet<>(userIds);
        
        for (OrderData data : orderDatabase.values()) {
            if (idSet.contains(data.userId)) {
                results.add(data);
            }
        }
        return results;
	}

}
