package cgx.com.usecase.ManageUser.DeleteUser;

import java.time.Instant;
import java.util.List;

import cgx.com.Entities.OrderStatus;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class DeleteUserUseCase implements DeleteUserInputBoundary{
	private final IAuthTokenValidator tokenValidator;
	private final IUserRepository userRepository;
	private IOrderRepository orderRepository;
	private final DeleteUserOutputBoundary outputBoundary;

    public DeleteUserUseCase(IAuthTokenValidator tokenValidator,
                                     IUserRepository userRepository,
                                     IOrderRepository orderRepository,
                                     DeleteUserOutputBoundary outputBoundary) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.outputBoundary = outputBoundary;
    }
    
    @Override
    public final void execute(DeleteUserRequestData input) {
        DeleteUserResponseData output = new DeleteUserResponseData();

        try {
            AuthPrincipal adminPrincipal = tokenValidator.validate(input.authToken);
            User.validateIsAdmin(adminPrincipal.role);

            UserData adminData = userRepository.findByUserId(adminPrincipal.userId);
            UserData targetUserData = userRepository.findByUserId(input.targetUserId);
            if (targetUserData == null) {
                throw new SecurityException("Không tìm thấy tài khoản người dùng.");
            }

            User adminEntity = mapToEntity(adminData);
            adminEntity.validateAdminSelfUpdate(input.targetUserId);
            
            checkActiveOrders(input.targetUserId);
            
            User userEntity = mapToEntity(targetUserData);
            // Xóa - Đặt trạng thái là deleted, không xóa cứng
            userEntity.softDelete();

            UserData userToSave = mapToData(userEntity);
            UserData savedData = userRepository.update(userToSave);

            // 7. Báo cáo thành công (Chung)
            output.success = true;
            output.message = "Xóa người dùng thành công.";
            output.deletedUserId = savedData.userId;
            output.newStatus = String.valueOf(savedData.status);

        } catch (SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (IllegalStateException e) {  
            output.success = false;
            output.message = e.getMessage();
        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

        outputBoundary.present(output);
    }
    
    private void checkActiveOrders(String userId) {
    	// Tìm danh sách đơn hàng của user từ Repository
        List<OrderData> userOrders = orderRepository.findByUserId(userId);
        
        if (userOrders != null && !userOrders.isEmpty()) {
            for (OrderData order : userOrders) {
                if (isActiveOrder(order)) {
                    throw new IllegalStateException("Không thể xóa người dùng đang có đơn hàng chưa hoàn tất (Mã đơn: " + order.id + ").");
                }
            }
        }
	}

	private boolean isActiveOrder(OrderData order) {
		if (order.status == null) return false;
        
        try {
            OrderStatus status = OrderStatus.valueOf(order.status);
            
            // DELIVERED, CANCELLED -> Đã xong -> Cho phép xóa
            return status == OrderStatus.PENDING || 
                   status == OrderStatus.CONFIRMED || 
                   status == OrderStatus.SHIPPED;
                   
        } catch (IllegalArgumentException e) {
            return false; 
        }
	}

	private UserData mapToData(User user) {
		return new UserData(
	            user.getUserId(),
	            user.getEmail(),
	            user.getHashedPassword(),
	            user.getFirstName(),
	            user.getLastName(),
	            user.getPhoneNumber(),
	            user.getRole(),
	            user.getStatus(),
	            user.getCreatedAt(),
	            user.getUpdatedAt()
	        );
	}

	private User mapToEntity(UserData user) {
		return new User(
				user.userId,
				user.email,
				user.hashedPassword,
				user.firstName,
				user.lastName,
				user.phoneNumber,
				user.role,
				user.status,
				user.createdAt,
				user.updatedAt
		);
	}
}
