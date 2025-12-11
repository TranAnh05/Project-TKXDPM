package cgx.com.usecase.ManageUser.VerifyPasswordReset;

public class VerifyPasswordResetRequestData extends BaseVerifyResetRequestData{
    public final String resetToken; 

    public VerifyPasswordResetRequestData(String resetToken, String newPassword) {
        super(newPassword);
        this.resetToken = resetToken;
    }
}
