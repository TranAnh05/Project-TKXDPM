package adapters.ManageUser.AuthenticateUser;

public class LoginSuccessViewDTO {
	public String token;
    public String userId;
    public String email;
    public String role; // Sẽ là "CUSTOMER" hoặc "ADMIN"
}
