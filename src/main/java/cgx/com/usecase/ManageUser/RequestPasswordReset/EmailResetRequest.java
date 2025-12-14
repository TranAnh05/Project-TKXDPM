package cgx.com.usecase.ManageUser.RequestPasswordReset;

public class EmailResetRequest extends ResetRequestData{
	public final String email;

    public EmailResetRequest(String email) {
        this.email = email;
    }
}
