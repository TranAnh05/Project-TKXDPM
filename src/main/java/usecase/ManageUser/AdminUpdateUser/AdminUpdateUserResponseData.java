package usecase.ManageUser.AdminUpdateUser;

import Entities.AccountStatus;
import Entities.UserRole;

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
