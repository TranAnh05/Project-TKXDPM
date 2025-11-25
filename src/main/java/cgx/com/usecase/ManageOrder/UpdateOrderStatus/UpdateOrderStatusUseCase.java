package cgx.com.usecase.ManageOrder.UpdateOrderStatus;

import java.time.Instant;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Order;
import cgx.com.Entities.OrderItem;
import cgx.com.Entities.OrderStatus;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public class UpdateOrderStatusUseCase implements UpdateOrderStatusInputBoundary {

    private final IOrderRepository orderRepository;
    private final IDeviceRepository deviceRepository; // Cần để hoàn kho nếu Admin hủy
    private final IAuthTokenValidator tokenValidator;
    private final IDeviceMapper deviceMapper; // Cần để map Device khi hoàn kho
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
            // 1. Validate Auth (Admin Only)
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            if (principal.role != UserRole.ADMIN) {
                throw new SecurityException("Không có quyền truy cập (Yêu cầu Admin).");
            }

            // 2. Validate Input
            Order.validateId(input.orderId);
            
            // Validate Status Enum
            OrderStatus newStatus;
            try {
                newStatus = OrderStatus.valueOf(input.newStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Trạng thái không hợp lệ: " + input.newStatus);
            }

            // 3. Tìm đơn hàng
            OrderData orderData = orderRepository.findById(input.orderId);
            if (orderData == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
            }

            // 4. Rehydrate Order Entity
            Order orderEntity = mapDataToOrder(orderData);

            // 5. Thực hiện Logic Nghiệp vụ trên Entity
            // Entity sẽ tự kiểm tra quy tắc chuyển đổi trạng thái (State Machine)
            // Nếu vi phạm, Entity sẽ ném IllegalStateException hoặc IllegalArgumentException
            orderEntity.updateStatus(newStatus);

            // 6. Xử lý Side-effect: Hoàn kho nếu Hủy đơn
            // (Đây là việc điều phối của Use Case, Entity không làm được vì không có Repo)
            if (newStatus == OrderStatus.CANCELLED) {
                restockItems(orderEntity);
            }

            // 7. Map ngược lại DTO để lưu
            orderData.status = orderEntity.getStatus().name();
            orderData.updatedAt = orderEntity.getUpdatedAt();
            
            orderRepository.save(orderData);

            // 8. Success
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

    // Helper Hoàn kho (Copy từ CancelOrderUseCase hoặc tách ra Shared Service)
    // Vì đây là 2 UseCase riêng biệt, việc copy code private là chấp nhận được để độc lập (Decoupling)
    private void restockItems(Order order) {
        for (OrderItem item : order.getItems()) {
            DeviceData deviceData = deviceRepository.findById(item.getDeviceId());
            if (deviceData != null) {
                ComputerDevice deviceEntity = deviceMapper.toEntity(deviceData);
                deviceEntity.plusStock(item.getQuantity());
                deviceRepository.save(deviceMapper.toDTO(deviceEntity));
            }
        }
    }

    // Helper Rehydrate (Copy từ CancelOrderUseCase)
    private Order mapDataToOrder(OrderData data) {
        Order order = new Order(data.id, data.userId, data.shippingAddress, OrderStatus.valueOf(data.status));
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