package cgx.com.usecase.Payment.ProcessPayment;

import cgx.com.Entities.Order;
import cgx.com.Entities.OrderStatus;
import cgx.com.Entities.PaymentMethod;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.Payment.IPaymentStrategy;
import cgx.com.usecase.Payment.PaymentResult;
import cgx.com.usecase.Payment.PaymentStrategyFactory;

public class ProcessPaymentUseCase implements ProcessPaymentInputBoundary {

    private final IOrderRepository orderRepository;
    private final IAuthTokenValidator tokenValidator;
    private final PaymentStrategyFactory paymentFactory;
    private final ProcessPaymentOutputBoundary outputBoundary;

    public ProcessPaymentUseCase(IOrderRepository orderRepository,
                                 IAuthTokenValidator tokenValidator,
                                 PaymentStrategyFactory paymentFactory,
                                 ProcessPaymentOutputBoundary outputBoundary) {
        this.orderRepository = orderRepository;
        this.tokenValidator = tokenValidator;
        this.paymentFactory = paymentFactory;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(ProcessPaymentRequestData input) {
        ProcessPaymentResponseData output = new ProcessPaymentResponseData();

        try {
            // Validate người dùng
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            // Validate Input
            Order.validateId(input.orderId);
            Order.validatePaymentStatus(input.selectedPaymentMethod);
            PaymentMethod newMethod = Order.convertToPaymentMethod(input.selectedPaymentMethod);

            // Tìm Order
            OrderData orderData = orderRepository.findById(input.orderId);
            
            Order orderEntity = mapToEntity(orderData);
            orderEntity.validatePayableStatus();
            
            // Đổi phương thức thanh toán
            orderEntity.changePaymentMethod(newMethod);

            // CHỌN CHIẾN LƯỢC THANH TOÁN & XỬ LÝ
            IPaymentStrategy strategy = paymentFactory.getStrategy(orderEntity.getPaymentMethod().name());
            PaymentResult result = strategy.process(orderEntity);

            if (orderEntity.getPaymentMethod() == PaymentMethod.COD) {
                orderEntity.updateStatus(OrderStatus.CONFIRMED);
                output.message = "Đặt hàng thành công. Vui lòng chuẩn bị tiền mặt.";
            } else {
                // Xử lý Online -> Vẫn giữ PENDING, trả về URL để client thanh toán tiếp
                output.message = "Vui lòng thực hiện thanh toán qua ngân hàng.";
            }

            orderData.status = orderEntity.getStatus().name();           
            orderData.paymentMethod = orderEntity.getPaymentMethod().name();
            
            orderRepository.save(orderData);

            output.success = true;
            output.paymentUrl = result.paymentUrl;
            output.transactionRef = result.transactionId;

        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống: " + e.getMessage();
            e.printStackTrace(); 
        }

        outputBoundary.present(output);
    }

	private Order mapToEntity(OrderData orderData) {
		return new Order(
                orderData.id, 
                orderData.userId, 
                orderData.shippingAddress, 
                OrderStatus.valueOf(orderData.status), 
                PaymentMethod.valueOf(orderData.paymentMethod), 
                orderData.totalAmount 
            );
	}
}