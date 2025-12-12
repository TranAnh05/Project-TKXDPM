package cgx.com.usecase.ManageOrder.CancelOrder;

import cgx.com.Entities.ComputerDevice;
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
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class CancelOrderUseCase implements CancelOrderInputBoundary {

    private final IOrderRepository orderRepository;
    private final IDeviceRepository deviceRepository;
    private final IUserRepository userRepository;
    private final IAuthTokenValidator tokenValidator;
    private final IDeviceMapper deviceMapper;
    private final CancelOrderOutputBoundary outputBoundary;

    public CancelOrderUseCase(IOrderRepository orderRepository,
                              IDeviceRepository deviceRepository,
                              IUserRepository userRepository,
                              IAuthTokenValidator tokenValidator,
                              IDeviceMapper deviceMapper,
                              CancelOrderOutputBoundary outputBoundary) {
        this.orderRepository = orderRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.tokenValidator = tokenValidator;
        this.deviceMapper = deviceMapper;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(CancelOrderRequestData input) {
        CancelOrderResponseData output = new CancelOrderResponseData();

        try {
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            Order.validateId(input.orderId);

            OrderData orderData = orderRepository.findById(input.orderId);
            if (orderData == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
            }
            
            UserData userData = userRepository.findByUserId(orderData.userId);
            User userEntity = mapToEntity(userData);
            // Là Admin hoặc là chủ đơn hàng
            userEntity.validateAccess(principal.userId, principal.role);
            
            Order orderEntity = mapDataToOrder(orderData);
            orderEntity.cancel();

            // Hoàn kho
            for (OrderItem item : orderEntity.getItems()) {
                DeviceData deviceData = deviceRepository.findById(item.getDeviceId());
                if (deviceData != null) {
                    ComputerDevice deviceEntity = deviceMapper.toEntity(deviceData);
                    deviceEntity.plusStock(item.getQuantity());
                    
                    deviceRepository.save(deviceMapper.toDTO(deviceEntity));
                }
            }

            orderData.status = orderEntity.getStatus().name();
            orderData.updatedAt = orderEntity.getUpdatedAt();
            
            orderRepository.save(orderData);

            output.success = true;
            output.message = "Hủy đơn hàng thành công.";
            output.orderId = orderData.id;
            output.status = String.valueOf(OrderStatus.CANCELLED);

        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
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

    private Order mapDataToOrder(OrderData data) {
        Order order = new Order(data.id, data.userId, data.shippingAddress, OrderStatus.valueOf(data.status), PaymentMethod.valueOf(data.paymentMethod), data.totalAmount);
        
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