package cgx.com.usecase.ManageOrder.ViewOrderDetail;

import cgx.com.Entities.Order;
import cgx.com.Entities.OrderItem;
import cgx.com.Entities.OrderStatus;
import cgx.com.Entities.PaymentMethod;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class ViewOrderDetailUseCase implements ViewOrderDetailInputBoundary {

    private final IOrderRepository orderRepository;
    private final IAuthTokenValidator tokenValidator;
    private final IUserRepository userRepository;
    private final ViewOrderDetailOutputBoundary outputBoundary;

    public ViewOrderDetailUseCase(IOrderRepository orderRepository,
                                  IAuthTokenValidator tokenValidator,
                                  IUserRepository userRepository,
                                  ViewOrderDetailOutputBoundary outputBoundary) {
        this.orderRepository = orderRepository;
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(ViewOrderDetailRequestData input) {
        ViewOrderDetailResponseData output = new ViewOrderDetailResponseData();

        try {
        	AuthPrincipal principal = tokenValidator.validate(input.authToken);
            
            Order.validateId(input.orderId);
            
            OrderData orderData = orderRepository.findById(input.orderId);
            
            if (orderData == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
            }
            
            UserData userData = userRepository.findByUserId(orderData.userId);
            User user = mapToEntity(userData);
            user.validateAccess(principal.userId, principal.role);
            
            output.success = true;
            output.message = "Lấy chi tiết đơn hàng thành công.";
            output.order = orderData;

        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống: " + e.getMessage();
        }

        outputBoundary.present(output);
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
