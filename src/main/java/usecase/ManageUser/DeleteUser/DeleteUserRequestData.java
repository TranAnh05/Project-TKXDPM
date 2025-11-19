package usecase.ManageUser.DeleteUser;

public class DeleteUserRequestData {
	public final String authToken; // Token của Admin
    public final String targetUserId; // ID của User bị xóa

    public DeleteUserRequestData(String authToken, String targetUserId) {
        this.authToken = authToken;
        this.targetUserId = targetUserId;
    }
}
