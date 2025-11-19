package usecase.ManageUser.AdminViewUserDetails;

public class AdminViewUserDetailsRequestData {
	public final String authToken; // Token của Admin
    public final String targetUserId; // ID của User bị xem
    
    public AdminViewUserDetailsRequestData(String authToken, String targetUserId) {
        this.authToken = authToken;
        this.targetUserId = targetUserId;
    }
}
