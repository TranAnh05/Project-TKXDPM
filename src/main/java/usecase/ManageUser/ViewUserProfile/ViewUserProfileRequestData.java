package usecase.ManageUser.ViewUserProfile;

public class ViewUserProfileRequestData {
	public String authToken; // Ví dụ: "Bearer eyJ..."

    public ViewUserProfileRequestData(String authToken) {
        this.authToken = authToken;
    }
}
