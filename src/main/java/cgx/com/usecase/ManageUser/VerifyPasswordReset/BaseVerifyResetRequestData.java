package cgx.com.usecase.ManageUser.VerifyPasswordReset;

public abstract class BaseVerifyResetRequestData {
	public final String newPassword;
	
    public BaseVerifyResetRequestData(String newPassword) {
        this.newPassword = newPassword;
    }
}
