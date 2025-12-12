package cgx.com.usecase.ManageOrder.PlaceOrder;


import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.Laptop;
import cgx.com.Entities.Mouse;
import cgx.com.Entities.Order;
import cgx.com.Entities.OrderItem;
import cgx.com.Entities.PaymentMethod;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageOrder.IOrderIdGenerator;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

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
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            // Kiểm tra dữ liệu đầu vào
            Order.validateOrderInfo(input.shippingAddress, input.cartItems);
            
            // Tạo ID
            String orderId = idGenerator.generate();
            
            // Khởi tạo đơn hàng
            Order orderEntity = new Order(orderId, principal.userId, input.shippingAddress, PaymentMethod.valueOf(input.paymentMethod));

            // Lặp qua các mặt hàng trong giỏ hàng
            for (Map.Entry<String, Integer> entry : input.cartItems.entrySet()) {
                String deviceId = entry.getKey();
                Integer quantity = entry.getValue();

                // Lấy DTO từ DB
                DeviceData deviceData = deviceRepository.findById(deviceId);
                if (deviceData == null) {
                    throw new IllegalArgumentException("Sản phẩm không tồn tại: " + deviceId);
                }

                // Chuyển DTO sang entity theo loại thiết bị
                ComputerDevice deviceEntity = deviceMapper.toEntity(deviceData);
                
                // Kiểm tra tồn kho
                deviceEntity.validateStock(quantity);
                // Trừ tồn kho
                deviceEntity.minusStock(quantity);

                // Thêm mặt hàng vào giỏ hàng
                orderEntity.addItem(new OrderItem(
                    deviceEntity.getId(), deviceEntity.getName(), deviceEntity.getThumbnail(),
                    deviceEntity.getPrice(), quantity
                ));

                // Lưu Device đã trừ kho
                DeviceData updatedDeviceData = deviceMapper.toDTO(deviceEntity);
                deviceRepository.save(updatedDeviceData);
            }

            // Lưu Order
            OrderData orderData = mapOrderToData(orderEntity);
            orderRepository.save(orderData);

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