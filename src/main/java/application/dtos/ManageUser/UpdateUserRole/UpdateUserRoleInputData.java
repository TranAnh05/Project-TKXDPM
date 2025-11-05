package application.dtos.ManageUser.UpdateUserRole;

public class UpdateUserRoleInputData {
	public int userIdToUpdate; // ID của người bị sửa
    public String newRole;    // Vai trò mới (dưới dạng String: "ADMIN")
    public int currentAdminId; // ID của Admin đang thực hiện (để kiểm tra logic)
    
    public UpdateUserRoleInputData() {}
    
    public UpdateUserRoleInputData(int userIdToUpdate, String newRole, int currentAdminId) {
        this.userIdToUpdate = userIdToUpdate;
        this.newRole = newRole;
        this.currentAdminId = currentAdminId;
    }
}
