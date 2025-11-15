package usecase.ManageUser.AuthenticateUser;

public class AuthenticateUserRequestData {
	public String email;
    public String password;
    // (Tương lai có thể thêm: public final String googleToken;)

    public AuthenticateUserRequestData(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
