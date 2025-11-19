package cgx.com.usecase.ManageUser.AuthenticateUser;

import cgx.com.Entities.UserRole;

public class AuthenticateUserResponseData {
	public boolean success;
    public String message;
    
    // Dữ liệu khi thành công
    public String token;
    public String userId;
    public String email;
    public UserRole role; // Presenter sẽ chuyển cái này thành String
}
