package cgx.com.usecase.ManageUser.AdminUpdateUser;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;

public class AdminUpdateUserResponseData {
	public boolean success;
    public String message;
    
    // Dữ liệu khi thành công
    public String userId;
    public String email;
    public String firstName;
    public String lastName;
    public String phoneNumber;
    public UserRole role;
    public AccountStatus status;
}
