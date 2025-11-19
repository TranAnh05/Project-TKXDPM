package cgx.com.adapters.ManageUser.ChangePassword;

import cgx.com.usecase.ManageUser.ChangePassword.ChangePasswordOutputBoundary;
import cgx.com.usecase.ManageUser.ChangePassword.ChangePasswordResponseData;

public class ChangePasswordPresenter implements ChangePasswordOutputBoundary{
	private ChangePasswordViewModel viewModel;

    public ChangePasswordPresenter(ChangePasswordViewModel viewModel) {
        this.viewModel = viewModel;
    }
	
	
	@Override
	public void present(ChangePasswordResponseData responseData) {
		viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;
	}

	public ChangePasswordViewModel getModel() {
        return this.viewModel;
    }
}
