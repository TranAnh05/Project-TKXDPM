package cgx.com.usecase.ManageOrder.CancelOrder;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Order;
import cgx.com.Entities.OrderItem;
import cgx.com.Entities.OrderStatus;
import cgx.com.Entities.PaymentMethod;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageOrder.PlaceOrder.IDeviceMapper;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public class CancelOrderUseCase implements CancelOrderInputBoundary {

    private final IOrderRepository orderRepository;
    private final IDeviceRepository deviceRepository;
    private final IAuthTokenValidator tokenValidator;
    private final IDeviceMapper deviceMapper;
    private final CancelOrderOutputBoundary outputBoundary;

    public CancelOrderUseCase(IOrderRepository orderRepository,
                              IDeviceRepository deviceRepository,
                              IAuthTokenValidator tokenValidator,
                              IDeviceMapper deviceMapper,
                              CancelOrderOutputBoundary outputBoundary) {
        this.orderRepository = orderRepository;
        this.deviceRepository = deviceRepository;
        this.tokenValidator = tokenValidator;
        this.deviceMapper = deviceMapper;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(CancelOrderRequestData input) {
        CancelOrderResponseData output = new CancelOrderResponseData();

        try {
            // 1. Validate Auth
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            // 2. Validate Input
            Order.validateId(input.orderId);

            // 3. Tìm đơn hàng (Lấy DTO)
            OrderData orderData = orderRepository.findById(input.orderId);
            if (orderData == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
            }

            // 4. Rehydrate Order Entity (Để xử lý logic trạng thái)
            Order orderEntity = mapDataToOrder(orderData);

            // 5. Kiểm tra Quyền (Chủ đơn hoặc Admin)
            if (principal.role != UserRole.ADMIN && !principal.userId.equals(orderEntity.getUserId())) {
                throw new SecurityException("Bạn không có quyền hủy đơn hàng này.");
            }

            // 6. Kiểm tra Trạng thái & Thực hiện Hủy (Logic trong Entity)
            // (Hàm cancel sẽ ném IllegalStateException nếu trạng thái không hợp lệ)
            orderEntity.cancel();

            // 7. HOÀN KHO (RESTOCK LOGIC)
            for (OrderItem item : orderEntity.getItems()) {
                // 7a. Tìm sản phẩm gốc
                DeviceData deviceData = deviceRepository.findById(item.getDeviceId());
                
                // Nếu sản phẩm còn tồn tại (chưa bị xóa cứng), thì hoàn kho
                if (deviceData != null) {
                    // Dùng Mapper để tái tạo Device Entity
                    ComputerDevice deviceEntity = deviceMapper.toEntity(deviceData);
                    
                    // 7b. Cộng lại số lượng (Logic trong Entity)
                    deviceEntity.plusStock(item.getQuantity());
                    
                    // 7c. Lưu lại sản phẩm
                    deviceRepository.save(deviceMapper.toDTO(deviceEntity));
                }
            }

            // 8. Cập nhật DTO từ Entity đã hủy
            orderData.status = orderEntity.getStatus().name();
            // (Nếu Entity có updateAt, cũng nên cập nhật lại vào DTO)
            orderData.updatedAt = orderEntity.getUpdatedAt();
            
            // 9. Lưu Đơn hàng
            orderRepository.save(orderData);

            // 10. Success
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

    /**
     * Helper để tái tạo Order Entity từ DTO (phục vụ logic check status).
     * Sử dụng constructor đầy đủ của Order (được thêm mới để hỗ trợ rehydrate).
     */
    private Order mapDataToOrder(OrderData data) {
        // Sử dụng constructor rehydrate mới (Status được lấy từ DB)
        Order order = new Order(data.id, data.userId, data.shippingAddress, OrderStatus.valueOf(data.status), PaymentMethod.valueOf(data.paymentMethod), data.totalAmount);
        
        // Tái tạo items để có thể lặp qua (cho logic hoàn kho)
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