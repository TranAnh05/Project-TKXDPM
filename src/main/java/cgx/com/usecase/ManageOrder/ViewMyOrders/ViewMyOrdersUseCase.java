package cgx.com.usecase.ManageOrder.ViewMyOrders;

import java.util.List;

import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;

public class ViewMyOrdersUseCase implements ViewMyOrdersInputBoundary {

    private final IOrderRepository orderRepository;
    private final IAuthTokenValidator tokenValidator;
    private final ViewMyOrdersOutputBoundary outputBoundary;

    public ViewMyOrdersUseCase(IOrderRepository orderRepository,
                               IAuthTokenValidator tokenValidator,
                               ViewMyOrdersOutputBoundary outputBoundary) {
        this.orderRepository = orderRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(ViewMyOrdersRequestData input) {
        ViewMyOrdersResponseData output = new ViewMyOrdersResponseData();

        try {
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            
            List<OrderData> orders = orderRepository.findByUserId(principal.userId);

            output.success = true;
            output.message = "Lấy danh sách đơn hàng thành công.";
            output.orders = orders;

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