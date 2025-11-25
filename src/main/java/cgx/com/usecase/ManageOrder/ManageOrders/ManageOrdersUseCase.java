package cgx.com.usecase.ManageOrder.ManageOrders;

import java.util.List;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageOrder.IOrderRepository;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.SearchUsers.PaginationData;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public class ManageOrdersUseCase implements ManageOrdersInputBoundary {

    private final IOrderRepository orderRepository;
    private final IAuthTokenValidator tokenValidator;
    private final ManageOrdersOutputBoundary outputBoundary;

    public ManageOrdersUseCase(IOrderRepository orderRepository,
                               IAuthTokenValidator tokenValidator,
                               ManageOrdersOutputBoundary outputBoundary) {
        this.orderRepository = orderRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(ManageOrdersRequestData input) {
        ManageOrdersResponseData output = new ManageOrdersResponseData();

        try {
            // 1. Validate Auth (Admin Only)
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            if (principal.role != UserRole.ADMIN) {
                throw new SecurityException("Không có quyền truy cập (Yêu cầu Admin).");
            }

            // 2. Xây dựng Criteria
            OrderSearchCriteria criteria = new OrderSearchCriteria(input.statusFilter, input.userIdFilter);

            // 3. Tìm kiếm (Phân trang)
            int pageIndex = input.page - 1; // DB 0-based
            List<OrderData> orders = orderRepository.search(criteria, pageIndex, input.size);

            // 4. Đếm tổng số
            long totalCount = orderRepository.count(criteria);

            // 5. Tạo Pagination Data
            PaginationData pagination = new PaginationData(totalCount, input.page, input.size);

            // 6. Success
            output.success = true;
            output.message = "Lấy danh sách đơn hàng thành công.";
            output.orders = orders;
            output.pagination = pagination;

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
