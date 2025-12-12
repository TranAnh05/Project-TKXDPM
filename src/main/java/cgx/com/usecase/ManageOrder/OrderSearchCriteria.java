package cgx.com.usecase.ManageOrder;

public class OrderSearchCriteria {
	public final String status;   // Lọc theo trạng thái (PENDING, SHIPPED...)
    public final String userId;   // Lọc theo khách hàng (Optional)
    // Có thể mở rộng thêm: fromDate, toDate, minAmount...

    public OrderSearchCriteria(String status, String userId) {
        this.status = status;
        this.userId = userId;
    }
}
