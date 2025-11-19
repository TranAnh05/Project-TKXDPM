package cgx.com.usecase.ManageUser.DeleteUser;

public class DeleteUserResponseData {
	public boolean success;
    public String message;
    
    // Dữ liệu khi thành công
    public String deletedUserId;
    public String newStatus; // Trả về "DELETED"
}
