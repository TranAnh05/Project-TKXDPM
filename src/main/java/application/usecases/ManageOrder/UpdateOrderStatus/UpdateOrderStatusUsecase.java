package application.usecases.ManageOrder.UpdateOrderStatus;

import application.dtos.ManageOrder.OrderData;
import application.dtos.ManageOrder.OrderOutputData;
import application.dtos.ManageOrder.UpdateOrderStatus.UpdateOrderStatusInputData;
import application.dtos.ManageOrder.UpdateOrderStatus.UpdateOrderStatusOutputData;
import application.dtos.ManageUser.UserData;
import application.ports.in.ManageOrder.UpdateOrderStatus.UpdateOrderStatusInputBoundary;
import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageOrder.UpdateOrderStatus.UpdateOrderStatusOutputBoundary;
import application.ports.out.ManageUser.UserRepository;
import domain.entities.Order;
import domain.entities.OrderStatus;

public class UpdateOrderStatusUsecase implements UpdateOrderStatusInputBoundary{
	private OrderRepository orderRepository;
    private UserRepository userRepository; // Cần để "làm giàu" DTO trả về
    private UpdateOrderStatusOutputBoundary orderPresenter;
    private UpdateOrderStatusOutputData outputData; // Field cho TDD
    
    public UpdateOrderStatusUsecase() {}
    
	public UpdateOrderStatusUsecase(OrderRepository orderRepository, UserRepository userRepository,
			UpdateOrderStatusOutputBoundary orderPresenter) {
		this.orderRepository = orderRepository;
		this.userRepository = userRepository;
		this.orderPresenter = orderPresenter;
	}

	public UpdateOrderStatusOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(UpdateOrderStatusInputData input) {
		outputData = new UpdateOrderStatusOutputData();
		
		try {
			OrderStatus newStatus;
			// 1. GỌI VALIDATION
			Order.validateStatus(input.newStatus);
			newStatus = OrderStatus.valueOf(input.newStatus.toUpperCase());
			
			// 2. Lấy OrderData (T3 DTO)
            OrderData orderData = orderRepository.findById(input.orderId);
            if (orderData == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn hàng để cập nhật.");
            }
            
            // 3. Chuyển T3 DTO -> T4 Entity
            Order orderEntity = mapDataToEntity(orderData);
            
            // 4. GỌI TẦNG 4 (Entity) ĐỂ CẬP NHẬT
            orderEntity.setStatus(newStatus);
            
            // 5. Chuyển T4 (Entity) -> T3 DTO
            OrderData dataToUpdate = mapEntityToData(orderEntity);
            
         // 6. Lưu vào CSDL
            OrderData updatedOrderData = orderRepository.update(dataToUpdate);

            // 7. Báo cáo thành công (Làm giàu DTO)
            outputData.success = true;
            outputData.message = "Cập nhật trạng thái thành công!";
            outputData.updatedOrder = mapToOutputData(updatedOrderData);
		} catch (IllegalArgumentException e) {
            // 8. BẮT LỖI VALIDATION (T4) HOẶC LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
        } catch (Exception e) {
        	// 9. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.";
		}
		
		orderPresenter.present(outputData);
	}

	private OrderOutputData mapToOutputData(OrderData data) {
		OrderOutputData dto = new OrderOutputData();
		
        dto.id = data.id;
        dto.userId = data.userId;
        dto.totalAmount = data.totalAmount;
        dto.status = data.status;
        dto.orderDate = data.orderDate;
        
        // Làm giàu (Enrich) User Email
        UserData user = userRepository.findById(data.userId);
        dto.userEmail = (user != null) ? user.email : "Người dùng đã bị xóa";
        
        return dto;
	}

	private OrderData mapEntityToData(Order entity) {
		return new OrderData(entity.getId(), entity.getUserId(), entity.getOrderDate(),
                entity.getTotalAmount(), entity.getStatus());
	}

	private Order mapDataToEntity(OrderData data) {
		return new Order(data.id, data.userId, data.orderDate, data.totalAmount, data.status);
	}

}
