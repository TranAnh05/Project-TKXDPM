package infrastructure.web.requests.UpdateOrderStatus;

/**
 * LỚP MỚI: DTO Tầng 1 (Request)
 * Lớp này được Gson dùng để map (ánh xạ) JSON request body cho việc "Sửa Status".
 * (Dùng public fields)
 */
public class UpdateOrderStatusRequest {
    // (Không cần ID, vì ID nằm trên URL)
    public String newStatus; // (Chỉ cần trạng thái mới, ví dụ: "SHIPPED")
	
}
