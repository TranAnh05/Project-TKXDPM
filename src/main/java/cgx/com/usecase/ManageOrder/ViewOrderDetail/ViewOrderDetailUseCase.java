package cgx.com.usecase.ManageOrder.ViewOrderDetail;

import cgx.com.Entities.Order;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
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
            // 1. Validate Auth
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }
            
            Order.validateId(input.orderId);

            // 2. Lấy thông tin người dùng
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            // 3. Tìm đơn hàng
            OrderData orderData = orderRepository.findById(input.orderId);
            if (orderData == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
            }

            // 4. Kiểm tra quyền truy cập (Bảo mật)
            // Nếu là Admin: Được xem hết
            // Nếu là Customer: Chỉ xem được đơn của chính mình
            if (principal.role != UserRole.ADMIN && !principal.userId.equals(orderData.userId)) {
                // Trả về thông báo chung để bảo mật, hoặc báo lỗi quyền
                throw new SecurityException("Bạn không có quyền xem đơn hàng này.");
            }

            // 5. Success
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
}
