package cgx.com.usecase.ManageOrder.PlaceOrder;


import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Laptop;
import cgx.com.Entities.Mouse;
import cgx.com.Entities.Order;
import cgx.com.Entities.OrderItem;
import cgx.com.Entities.PaymentMethod;
import cgx.com.usecase.ManageOrder.IOrderIdGenerator;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

import java.util.ArrayList;
import java.util.Map;

public class PlaceOrderUseCase implements PlaceOrderInputBoundary {

    private final IOrderRepository orderRepository;
    private final IDeviceRepository deviceRepository;
    private final IAuthTokenValidator tokenValidator;
    private final IOrderIdGenerator idGenerator;
    private final IDeviceMapper deviceMapper;
    private final PlaceOrderOutputBoundary outputBoundary;

    public PlaceOrderUseCase(IOrderRepository orderRepository,
                             IDeviceRepository deviceRepository,
                             IAuthTokenValidator tokenValidator,
                             IOrderIdGenerator idGenerator,
                             IDeviceMapper deviceMapper,
                             PlaceOrderOutputBoundary outputBoundary) {
        this.orderRepository = orderRepository;
        this.deviceRepository = deviceRepository;
        this.tokenValidator = tokenValidator;
        this.idGenerator = idGenerator;
        this.deviceMapper = deviceMapper;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(PlaceOrderRequestData input) {
        PlaceOrderResponseData output = new PlaceOrderResponseData();

        try {
            // 1. Validate Auth & Input Sơ bộ (Null check)
            if (input.authToken == null) throw new SecurityException("Auth Token không được để trống.");
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            // 2. Khởi tạo Order (Entity sẽ tự validate address, userId)
            String orderId = idGenerator.generate();
            // NẾU address rỗng -> Entity ném IllegalArgumentException -> Catch bên dưới
            Order.validateOrderInfo(input.shippingAddress, principal.userId, input.cartItems, input.paymentMethod);
            Order orderEntity = new Order(orderId, principal.userId, input.shippingAddress, PaymentMethod.valueOf(input.paymentMethod));

            for (Map.Entry<String, Integer> entry : input.cartItems.entrySet()) {
                String deviceId = entry.getKey();
                Integer quantity = entry.getValue();

                // 3a. Lấy DTO từ DB
                DeviceData deviceData = deviceRepository.findById(deviceId);
                if (deviceData == null) {
                    throw new IllegalArgumentException("Sản phẩm không tồn tại: " + deviceId);
                }

                // 3b. Tái tạo Device Entity (Mapper tự xử lý OCP)
                ComputerDevice deviceEntity = deviceMapper.toEntity(deviceData);
                
                // 3c. Trừ tồn kho (Entity Device tự validate số lượng âm)
                // Lấy tồn kho hiện tại trừ đi số lượng mua
                deviceEntity.validateStock(quantity);
                deviceEntity.minusStock(quantity);

                // 3d. Thêm vào Order Entity
                orderEntity.addItem(new OrderItem(
                    deviceEntity.getId(), deviceEntity.getName(), deviceEntity.getThumbnail(),
                    deviceEntity.getPrice(), quantity
                ));

                // 3e. Lưu Device đã trừ kho
                DeviceData updatedDeviceData = deviceMapper.toDTO(deviceEntity);
                deviceRepository.save(updatedDeviceData);
            }

            // 4. Lưu Order
            OrderData orderData = mapOrderToData(orderEntity);
            orderRepository.save(orderData);

            // 5. Success
            output.success = true;
            output.message = "Đặt hàng thành công!";
            output.orderId = orderEntity.getId();
            output.totalAmount = orderEntity.getTotalAmount();

        } catch (IllegalArgumentException | SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống: " + e.getMessage();
        }

        outputBoundary.present(output);
    }

    private OrderData mapOrderToData(Order entity) {
        OrderData data = new OrderData();
        data.id = entity.getId();
        data.userId = entity.getUserId();
        data.totalAmount = entity.getTotalAmount();
        data.status = entity.getStatus().name();
        data.shippingAddress = entity.getShippingAddress();
        data.paymentMethod = entity.getPaymentMethod().name();
        data.createdAt = entity.getCreatedAt();
        data.updatedAt = entity.getUpdatedAt();
        
        data.items = new ArrayList<>();
        for (OrderItem item : entity.getItems()) {
            data.items.add(new OrderItemData(
                item.getDeviceId(), item.getDeviceName(), item.getThumbnail(), item.getUnitPrice(), item.getQuantity()
            ));
        }
        return data;
    }
}