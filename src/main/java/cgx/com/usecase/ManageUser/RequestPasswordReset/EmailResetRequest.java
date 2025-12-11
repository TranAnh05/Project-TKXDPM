package cgx.com.usecase.ManageUser.RequestPasswordReset;

public class EmailResetRequest implements ResetRequestData{
	public final String email;

    public EmailResetRequest(String email) {
        this.email = email;
    }
}
