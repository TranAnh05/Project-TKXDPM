package cgx.com.usecase.ManageUser.AdminCreateNewUser;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;

public class AdminCreateUserResponseData {
	public boolean success;
	public String message;
	    
	// Dữ liệu khi thành công
    public String createdUserId;
    public String email;
    public String fullName;
    public UserRole role;
    public AccountStatus status;
}
