package cgx.com.usecase.ManageUser.AdminUpdateUser;

public class AdminUpdateUserRequestData {
	public String authToken; // Token của Admin
    public String targetUserId; // ID của User bị sửa
    
    // Dữ liệu mới
    public String email;
    public String firstName;
    public String lastName;
    public String phoneNumber;
    public String role;   
    public String status; 

    public AdminUpdateUserRequestData() {}
    
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
