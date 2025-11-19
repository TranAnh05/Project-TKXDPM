package cgx.com.adapters.ManageUser.VerifyPasswordReset;

import cgx.com.usecase.ManageUser.VerifyPasswordReset.VerifyPasswordResetOutputBoundary;
import cgx.com.usecase.ManageUser.VerifyPasswordReset.VerifyPasswordResetResponseData;

public class VerifyPasswordResetPresenter implements VerifyPasswordResetOutputBoundary{
	private VerifyPasswordResetViewModel viewModel;

    public VerifyPasswordResetPresenter(VerifyPasswordResetViewModel viewModel) {
        this.viewModel = viewModel;
    }
	
	@Override
	public void present(VerifyPasswordResetResponseData responseData) {
		viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;
	}

	public VerifyPasswordResetViewModel getModel() {
        return this.viewModel;
    }
}
