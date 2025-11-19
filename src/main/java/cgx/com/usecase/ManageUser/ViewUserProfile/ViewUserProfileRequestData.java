package cgx.com.usecase.ManageUser.ViewUserProfile;

public class ViewUserProfileRequestData {
	public final String authToken; // Ví dụ: "Bearer eyJ..."
    public final String targetUserId; // ID của user mà Admin muốn xem (có thể là null)

    /**
     * Constructor cho Customer (UC-4: ViewOwnProfile)
     */
    public ViewUserProfileRequestData(String authToken) {
        this(authToken, null);
    }

    /**
     * Constructor cho Admin (UC-A1: AdminViewUserDetails)
     */
    public ViewUserProfileRequestData(String authToken, String targetUserId) {
        this.authToken = authToken;
        this.targetUserId = targetUserId;
    }
}
