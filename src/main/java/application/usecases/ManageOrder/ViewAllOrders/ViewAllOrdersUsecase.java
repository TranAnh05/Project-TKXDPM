package application.usecases.ManageOrder.ViewAllOrders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Entities.Order;
import application.dtos.ManageOrder.OrderData;
import application.dtos.ManageOrder.OrderOutputData;
import application.dtos.ManageOrder.ViewAllOrders.ViewAllOrdersOutputData;
import application.ports.in.ManageOrder.ViewAllOrders.ViewAllOrdersInputBoundary;
import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageOrder.ViewAllOrders.ViewAllOrdersOutputBoundary;
import application.ports.out.ManageUser.UserRepository;
import usecase.ManageUser.UserData;

public class ViewAllOrdersUsecase implements ViewAllOrdersInputBoundary{
	private OrderRepository orderRepository;
    private UserRepository userRepository;
    private ViewAllOrdersOutputBoundary orderPresenter;
    private ViewAllOrdersOutputData outputData; // Field cho TDD
    
    public ViewAllOrdersUsecase() {}
    
	public ViewAllOrdersUsecase(OrderRepository orderRepository, UserRepository userRepository,
			ViewAllOrdersOutputBoundary orderPresenter) {
		this.orderRepository = orderRepository;
		this.userRepository = userRepository;
		this.orderPresenter = orderPresenter;
	}
	
	public ViewAllOrdersOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute() {
		outputData = new ViewAllOrdersOutputData();
		
		try {
			// 1. Lấy OrderData (T3 DTO)
            List<OrderData> orderDataList = orderRepository.findAll();
            if (orderDataList.isEmpty()) {
                outputData.success = true;
                outputData.message = "Chưa có đơn hàng nào.";
                outputData.orders = new ArrayList<>();
                orderPresenter.present(outputData);
                return;
            }

            // 2. Lấy UserData (T3 DTO) để tạo Map tra cứu
            Map<Integer, UserData> userMap = mapUsersToData(userRepository.findAll());

            // 3. Chuyển DTO (T3) -> Entity (T4)
            List<Order> orderEntities = mapDataToEntities(orderDataList);
            
            // 4. Chuyển Entity (T4) -> Output DTO an toàn (T3)
            // (Bước này "làm giàu" thêm userEmail)
            List<OrderOutputData> safeOutputList = mapEntitiesToOutputData(orderEntities, userMap);

            // 5. Báo cáo thành công
            outputData.success = true;
            outputData.orders = safeOutputList;
		} catch (Exception e) {
			// 6. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống khi tải đơn hàng.";
            outputData.orders = new ArrayList<>();
		}
		
		orderPresenter.present(outputData);
	}

	private List<OrderOutputData> mapEntitiesToOutputData(List<Order> entities, Map<Integer, UserData> userMap) {
		List<OrderOutputData> dtoList = new ArrayList<>();
		
        for (Order entity : entities) {
            OrderOutputData dto = new OrderOutputData();
            dto.id = entity.getId();
            dto.userId = entity.getUserId();
            dto.orderDate = entity.getOrderDate();
            dto.totalAmount = entity.getTotalAmount();
            dto.status = entity.getStatus();
            
            // 2. Làm giàu (Enrich) dữ liệu User
            UserData user = userMap.get(entity.getUserId());
            dto.userEmail = (user != null) ? user.email : "Người dùng đã bị xóa"; // Logic phòng thủ
            
            dtoList.add(dto);
        }
        
        return dtoList;
	}

	private List<Order> mapDataToEntities(List<OrderData> dataList) {
		List<Order> entities = new ArrayList<>();
        for (OrderData data : dataList) {
            entities.add(new Order(
                data.id, data.userId, data.orderDate, data.totalAmount, data.status
            ));
        }
        
        return entities;
	}

	private Map<Integer, UserData> mapUsersToData(List<UserData> dataList) {
		Map<Integer, UserData> map = new HashMap<>();
        for (UserData data : dataList) { map.put(data.id, data); }
        return map;
	}

}
