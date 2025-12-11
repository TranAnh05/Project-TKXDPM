package cgx.com.usecase.ManageUser.RequestPasswordReset;

public interface RequestPasswordResetInputBoundary<T extends ResetRequestData> {
	void execute(T input);
}
