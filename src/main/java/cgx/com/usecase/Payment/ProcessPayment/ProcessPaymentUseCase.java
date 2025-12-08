package cgx.com.usecase.Payment.ProcessPayment;

import cgx.com.Entities.Order;
import cgx.com.Entities.OrderStatus;
import cgx.com.Entities.PaymentMethod;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

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
            // 1. Validate Auth
            if (input.authToken == null) throw new SecurityException("Auth Token không được để trống.");
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            // 2. Validate Input
            Order.validateId(input.orderId);

            // 3. Tìm Order
            OrderData orderData = orderRepository.findById(input.orderId);
            if (orderData == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
            }

            // 4. Rehydrate Order Entity (Để check quyền sở hữu)
            // (Giả sử ta có map đơn giản ở đây hoặc dùng mapper như bài trước)
            // Check quyền sở hữu:
            if (principal.role != UserRole.ADMIN && !principal.userId.equals(orderData.userId)) {
                throw new SecurityException("Bạn không có quyền thanh toán cho đơn hàng này.");
            }
            
            if (!"PENDING".equals(orderData.status)) {
                throw new IllegalStateException("Đơn hàng không ở trạng thái chờ thanh toán.");
            }
            
         // ---------------------------------------------------------
            // 5b. LOGIC MỚI: CẬP NHẬT PHƯƠNG THỨC THANH TOÁN (NẾU NGƯỜI DÙNG ĐỔI Ý)
            // ---------------------------------------------------------
            String finalPaymentMethodName = orderData.paymentMethod; // Mặc định lấy cái cũ trong DB

            // Nếu user gửi lên phương thức mới và nó khác cái cũ
            if (input.selectedPaymentMethod != null && !input.selectedPaymentMethod.isEmpty()) {
                if (!input.selectedPaymentMethod.equals(orderData.paymentMethod)) {
                    // Validate xem phương thức mới có hợp lệ không
                    try {
                        PaymentMethod.valueOf(input.selectedPaymentMethod);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Phương thức thanh toán mới không hợp lệ.");
                    }
                    // Cập nhật biến tạm để tí nữa tạo Entity xử lý
                    finalPaymentMethodName = input.selectedPaymentMethod;
                }
            }

            // 6. Tái tạo Entity (Full Rehydration)
            // Dùng finalPaymentMethodName (có thể là cái mới hoặc cái cũ)
            PaymentMethod methodEnum = PaymentMethod.valueOf(finalPaymentMethodName);
            
            Order orderEntity = new Order(
                orderData.id, 
                orderData.userId, 
                orderData.shippingAddress, 
                OrderStatus.valueOf(orderData.status), 
                methodEnum, 
                orderData.totalAmount // Lấy đúng số tiền từ DB lên
            );

            // 7. CHỌN CHIẾN LƯỢC THANH TOÁN & XỬ LÝ
            IPaymentStrategy strategy = paymentFactory.getStrategy(orderEntity.getPaymentMethod().name());
            PaymentResult result = strategy.process(orderEntity);

            // 8. XỬ LÝ KẾT QUẢ & LƯU VÀO DB
            if (result.isSuccessful) {
                // A. Xử lý riêng cho COD (Tiền mặt) -> Chốt đơn luôn
                if (orderEntity.getPaymentMethod() == PaymentMethod.COD) {
                    orderEntity.updateStatus(OrderStatus.CONFIRMED);
                    output.message = "Đặt hàng thành công. Vui lòng chuẩn bị tiền mặt.";
                } else {
                    // B. Xử lý Online -> Vẫn giữ PENDING, trả về URL để client thanh toán tiếp
                    output.message = "Vui lòng thực hiện thanh toán qua ngân hàng.";
                }

                // --- QUAN TRỌNG: LƯU THAY ĐỔI XUỐNG DB ---
                // Cập nhật các trường có thể đã thay đổi vào DTO
                orderData.status = orderEntity.getStatus().name();           // Cập nhật Status (nếu là COD)
                orderData.paymentMethod = orderEntity.getPaymentMethod().name(); // Cập nhật Method (nếu user đổi ý)
                
                // Save thực sự
                orderRepository.save(orderData);
                // -----------------------------------------

                output.success = true;
                output.paymentUrl = result.paymentUrl;
                output.transactionRef = result.transactionId;
            } else {
                 throw new RuntimeException("Khởi tạo thanh toán thất bại.");
            }

        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống: " + e.getMessage();
            e.printStackTrace(); // Log lỗi ra console để debug
        }

        outputBoundary.present(output);
    }
}