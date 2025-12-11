package cgx.com.usecase.ManageUser.VerifyPasswordReset;

public interface VerifyPasswordResetInputBoundary<T extends BaseVerifyResetRequestData> {
	void execute(T inputData);
}
