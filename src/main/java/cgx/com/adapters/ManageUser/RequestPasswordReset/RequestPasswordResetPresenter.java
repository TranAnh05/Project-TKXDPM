package cgx.com.adapters.ManageUser.RequestPasswordReset;

import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestPasswordResetOutputBoundary;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestPasswordResetResponseData;

public class RequestPasswordResetPresenter implements RequestPasswordResetOutputBoundary{
	private RequestPasswordResetViewModel viewModel;

    public RequestPasswordResetPresenter(RequestPasswordResetViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
	@Override
	public void present(RequestPasswordResetResponseData responseData) {
		viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;
	}
	
	public RequestPasswordResetViewModel getModel() {
        return this.viewModel;
    }

}
