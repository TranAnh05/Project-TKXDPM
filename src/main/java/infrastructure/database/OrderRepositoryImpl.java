package infrastructure.database;

import java.util.List;

import application.dtos.ManageOrder.OrderData;
import application.ports.out.ManageOrder.OrderRepository;

public class OrderRepositoryImpl implements OrderRepository{

	@Override
	public boolean isProductInAnyOrder(int productId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OrderData findById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderData> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderData update(OrderData orderData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderData save(OrderData orderData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderData> findAllByUserIds(List<Integer> userIds) {
		// TODO Auto-generated method stub
		return null;
	}

}
