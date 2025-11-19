package usecase.ManageUser.UpdateUserProfile;

public class UpdateUserProfileRequestData {
	public final String authToken; // Token của người dùng
    
    // Dữ liệu mới
    public final String firstName;
    public final String lastName;
    public final String phoneNumber;

    public UpdateUserProfileRequestData(String authToken, String firstName, String lastName, String phoneNumber) {
        this.authToken = authToken;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }
}
