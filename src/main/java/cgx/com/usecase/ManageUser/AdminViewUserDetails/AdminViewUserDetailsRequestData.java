package cgx.com.usecase.ManageUser.AdminViewUserDetails;

public class AdminViewUserDetailsRequestData {
	public String authToken; // Token của Admin
    public String targetUserId; // ID của User bị xem
    
    public AdminViewUserDetailsRequestData() {}
    
    public AdminViewUserDetailsRequestData(String authToken, String targetUserId) {
        this.authToken = authToken;
        this.targetUserId = targetUserId;
    }
}
