package cgx.com.usecase.ManageUser.AdminUpdateUser;

public class AdminUpdateUserRequestData {
	public final String authToken; // Token của Admin
    public final String targetUserId; // ID của User bị sửa
    
    // Dữ liệu mới
    public final String email;
    public final String firstName;
    public final String lastName;
    public final String phoneNumber;
    public final String role;   // Dạng String (ví dụ: "CUSTOMER")
    public final String status; // Dạng String (ví dụ: "SUSPENDED")

    public AdminUpdateUserRequestData(String authToken, String targetUserId, String email, 
                                      String firstName, String lastName, String phoneNumber, 
                                      String role, String status) {
        this.authToken = authToken;
        this.targetUserId = targetUserId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
    }
}
