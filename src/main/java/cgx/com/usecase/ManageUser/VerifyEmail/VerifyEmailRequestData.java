package cgx.com.usecase.ManageUser.VerifyEmail;

public class VerifyEmailRequestData {
    public final String token; 

    public VerifyEmailRequestData(String token) {
        this.token = token;
    }
}
