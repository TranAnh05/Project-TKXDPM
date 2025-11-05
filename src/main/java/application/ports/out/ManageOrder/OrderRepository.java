package application.ports.out.ManageOrder;

import java.util.List;

import application.dtos.ManageOrder.OrderData;

public interface OrderRepository {
	// Dùng cho DeleteProductUseCase
    boolean isProductInAnyOrder(int productId);
    
    OrderData findById(int id);
    List<OrderData> findAll();
    OrderData update(OrderData orderData);
    OrderData save(OrderData orderData);
    
    List<OrderData> findAllByUserIds(List<Integer> userIds);
}
