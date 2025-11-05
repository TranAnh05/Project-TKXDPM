package application.usecases.SearchOrders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.dtos.ManageOrder.OrderData;
import application.dtos.ManageOrder.OrderOutputData;
import application.dtos.ManageUser.UserData;
import application.dtos.SearchOrders.SearchOrdersInputData;
import application.dtos.SearchOrders.SearchOrdersOutputData;
import application.ports.in.SearchOrders.SearchOrdersInputBoundary;
import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageUser.UserRepository;
import application.ports.out.SearchOrders.SearchOrdersOutputBoundary;
import domain.entities.Order;

public class SearchOrdersUsecase implements SearchOrdersInputBoundary{
	private OrderRepository orderRepository;
    private UserRepository userRepository;
    private SearchOrdersOutputBoundary orderPresenter;
    private SearchOrdersOutputData outputData; // Field cho TDD
    
    public SearchOrdersUsecase() {
    	
    }
    
	public SearchOrdersUsecase(OrderRepository orderRepository, UserRepository userRepository,
			SearchOrdersOutputBoundary orderPresenter) {
		this.orderRepository = orderRepository;
		this.userRepository = userRepository;
		this.orderPresenter = orderPresenter;
	}

	public SearchOrdersOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(SearchOrdersInputData input) {
		outputData = new SearchOrdersOutputData();
		
		try {
			// 1. Tìm User (T3 DTO) bằng email
			List<UserData> usersFound = userRepository.searchByEmail(input.emailKeyword);
			
			if (usersFound.isEmpty()) {
                // 2a. Xử lý: Không tìm thấy User
                outputData.success = true;
                outputData.message = "Không tìm thấy người dùng nào khớp với '" + input.emailKeyword + "'.";
                outputData.orders = new ArrayList<>();
                orderPresenter.present(outputData);
                return;
            }
			
			// 3. Chuẩn bị Map<Integer, UserData> (T3 DTO) để tra cứu (làm giàu)
            Map<Integer, UserData> userMap = mapUsersToData(usersFound);
            
            // 4. Lấy danh sách ID của User
            List<Integer> userIds = new ArrayList<>(userMap.keySet());
            
            // 5. Lấy OrderData (T3 DTO)
            List<OrderData> orderDataList = orderRepository.findAllByUserIds(userIds);
            
            if (orderDataList.isEmpty()) {
                // 2b. Xử lý: Tìm thấy User, nhưng User không có đơn hàng
                outputData.success = true;
                outputData.message = "Tìm thấy " + usersFound.size() + " người dùng, nhưng họ không có đơn hàng nào.";
                outputData.orders = new ArrayList<>();
                orderPresenter.present(outputData);
                return;
            }
            
            // 6. Chuyển DTO (T3) -> Entity (T4)
            List<Order> orderEntities = mapDataToEntities(orderDataList);
            
            // 7. Chuyển Entity (T4) -> Output DTO an toàn (T3) (làm giàu email)
            List<OrderOutputData> safeOutputList = mapEntitiesToOutputData(orderEntities, userMap);
            
            // 8. Báo cáo thành công
            outputData.success = true;
            outputData.orders = safeOutputList;
		} catch (Exception e) {
			// 9. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống khi tìm kiếm đơn hàng.";
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
            UserData user = userMap.get(entity.getUserId());
            dto.userEmail = (user != null) ? user.email : "Người dùng đã bị xóa";
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
