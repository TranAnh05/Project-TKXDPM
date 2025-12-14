package cgx.com.usecase.ManageUser.VerifyPasswordReset;

public class BaseVerifyResetRequestData {
	public final String newPassword;
	
    public BaseVerifyResetRequestData(String newPassword) {
        this.newPassword = newPassword;
    }
}
