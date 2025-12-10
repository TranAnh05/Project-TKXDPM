package cgx.com.adapters.ManageUser.VerifyEmail;

import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailOutputBoundary;
import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailResponseData;

public class VerifyEmailPresenter implements VerifyEmailOutputBoundary {

    private final VerifyEmailViewModel viewModel;
    
    public VerifyEmailPresenter(VerifyEmailViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(VerifyEmailResponseData output) {
        this.viewModel.success = String.valueOf(output.success);
        this.viewModel.message = output.message;
    }
    
    public VerifyEmailViewModel getViewModel() {
        return this.viewModel;
    }
}