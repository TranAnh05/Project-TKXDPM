package cgx.com.usecase.ManageOrder.ViewOrderDetail;

import cgx.com.Entities.Order;
import cgx.com.Entities.OrderItem;
import cgx.com.Entities.OrderStatus;
import cgx.com.Entities.PaymentMethod;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

public class ViewOrderDetailUseCase implements ViewOrderDetailInputBoundary {

    private final IOrderRepository orderRepository;
    private final IAuthTokenValidator tokenValidator;
    private final ViewOrderDetailOutputBoundary outputBoundary;

    public ViewOrderDetailUseCase(IOrderRepository orderRepository,
                                  IAuthTokenValidator tokenValidator,
                                  ViewOrderDetailOutputBoundary outputBoundary) {
        this.orderRepository = orderRepository;
        this.tokenValidator = tokenValidator;
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
            
            Order orderEntity = mapToEntity(orderData);
            
            orderEntity.validateAccess(principal.userId, principal.role);

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

	private Order mapToEntity(OrderData data) {
		Order order = new Order(
	            data.id,
	            data.userId,
	            data.shippingAddress,
	            OrderStatus.valueOf(data.status),
	            PaymentMethod.valueOf(data.paymentMethod),
	            data.totalAmount
	        );
	        
        if (data.items != null) {
            for (OrderItemData itemData : data.items) {
                order.addItem(new OrderItem(
                    itemData.deviceId, itemData.deviceName, itemData.thumbnail, 
                    itemData.unitPrice, itemData.quantity
                ));
            }
        }
        return order;
    }
}
