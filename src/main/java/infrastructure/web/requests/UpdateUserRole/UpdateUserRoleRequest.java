package infrastructure.web.requests.UpdateUserRole;

public class UpdateUserRoleRequest {
	// (Không cần ID, vì ID nằm trên URL)
    public String newRole; // (Chỉ cần vai trò mới)
    
    // (LƯU Ý: Chúng ta sẽ lấy currentAdminId từ Session/Token sau,
    //  hiện tại, chúng ta sẽ hard-code (gán cứng) ID 1 trong Servlet để test)
}
