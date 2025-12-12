package cgx.com.usecase.ManageOrder.UpdateOrderStatus;

import java.time.Instant;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Order;
import cgx.com.Entities.OrderItem;
import cgx.com.Entities.OrderStatus;
import cgx.com.Entities.PaymentMethod;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

public class UpdateOrderStatusUseCase implements UpdateOrderStatusInputBoundary {

    private final IOrderRepository orderRepository;
    private final IDeviceRepository deviceRepository; 
    private final IAuthTokenValidator tokenValidator;
    private final IDeviceMapper deviceMapper; 
    private final UpdateOrderStatusOutputBoundary outputBoundary;

    public UpdateOrderStatusUseCase(IOrderRepository orderRepository,
                                    IDeviceRepository deviceRepository,
                                    IAuthTokenValidator tokenValidator,
                                    IDeviceMapper deviceMapper,
                                    UpdateOrderStatusOutputBoundary outputBoundary) {
        this.orderRepository = orderRepository;
        this.deviceRepository = deviceRepository;
        this.tokenValidator = tokenValidator;
        this.deviceMapper = deviceMapper;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(UpdateOrderStatusRequestData input) {
        UpdateOrderStatusResponseData output = new UpdateOrderStatusResponseData();

        try {
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            User.validateIsAdmin(principal.role);

            Order.validateId(input.orderId);
            Order.validateOrderStatus(input.newStatus);
            OrderStatus newStatus = Order.convertToOrderStatus(input.newStatus);

            // Tìm đơn hàng
            OrderData orderData = orderRepository.findById(input.orderId);
            if (orderData == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
            }
            
            Order orderEntity = mapDataToOrder(orderData);
            
            orderEntity.updateStatus(newStatus);

            // Hoàn kho nếu hủy đơn
            if (newStatus == OrderStatus.CANCELLED) {
                restockItems(orderEntity);
            }

            orderData.status = orderEntity.getStatus().name();
            orderData.updatedAt = orderEntity.getUpdatedAt();
            
            orderRepository.save(orderData);

            output.success = true;
            output.message = "Cập nhật trạng thái thành công.";
            output.orderId = orderData.id;
            output.status = orderData.status;

        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống: " + e.getMessage();
        }

        outputBoundary.present(output);
    }

    // Duyệt qua từng loại thiết bị trong đơn hàng bị hủy để hoàn kho
    private void restockItems(Order order) {
        for (OrderItem item : order.getItems()) {
            DeviceData deviceData = deviceRepository.findById(item.getDeviceId());
            if (deviceData != null) {
                ComputerDevice deviceEntity = deviceMapper.toEntity(deviceData);
                // Hoàn kho
                deviceEntity.plusStock(item.getQuantity());
                deviceRepository.save(deviceMapper.toDTO(deviceEntity));
            }
        }
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