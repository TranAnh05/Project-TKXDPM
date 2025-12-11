package cgx.com.usecase.ManageUser.AuthenticateUser;

import cgx.com.Entities.UserRole;

public class AuthenticateUserResponseData {
	public boolean success;
    public String message;
    public String token;
    public String userId;
    public String email;
    public UserRole role; 
}
