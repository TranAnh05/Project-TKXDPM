package cgx.com.adapters.ManageUser.AdminViewUserDetails;

public class AdminManagedUserViewDTO {
	public String id;
    public String email;
    public String firstName;
    public String lastName;
    public String phoneNumber; // Sẽ là "" nếu null
    public String role;        // Dành cho Admin
    public String status;      // Dành cho Admin
}	
