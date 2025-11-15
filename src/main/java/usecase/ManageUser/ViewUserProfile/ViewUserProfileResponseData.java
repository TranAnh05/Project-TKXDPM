package usecase.ManageUser.ViewUserProfile;

public class ViewUserProfileResponseData {
	public boolean success;
    public String message;
    
    // Dữ liệu khi thành công
    public String userId;
    public String email;
    public String firstName;
    public String lastName;
    public String phoneNumber; // Có thể là null
}
