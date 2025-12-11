package cgx.com.usecase.ManageUser.DeleteUser;

public class DeleteUserRequestData {
	public String authToken; // Token của Admin
    public String targetUserId; // ID của User bị xóa

    public DeleteUserRequestData() {}
    
    public DeleteUserRequestData(String authToken, String targetUserId) {
        this.authToken = authToken;
        this.targetUserId = targetUserId;
    }
}
