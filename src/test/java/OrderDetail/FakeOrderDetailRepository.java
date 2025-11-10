package OrderDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.dtos.ManageOrder.OrderDetailData;
import application.ports.out.ManageOrder.OrderDetailRepository;

public class FakeOrderDetailRepository implements OrderDetailRepository{
	private Map<Integer, OrderDetailData> database = new HashMap<>();
	
	@Override
	public List<OrderDetailData> findAllByOrderId(int orderId) {
		List<OrderDetailData> results = new ArrayList<>();
        for (OrderDetailData detail : database.values()) {
            if (detail.orderId == orderId) {
                results.add(detail);
            }
        }
        return results;
	}
	
	public void save(OrderDetailData detail) {
        database.put(detail.id, detail);
    }
}
