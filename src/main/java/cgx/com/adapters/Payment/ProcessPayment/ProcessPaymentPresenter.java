package cgx.com.adapters.Payment.ProcessPayment;

import cgx.com.usecase.Payment.ProcessPayment.ProcessPaymentOutputBoundary;
import cgx.com.usecase.Payment.ProcessPayment.ProcessPaymentResponseData;

public class ProcessPaymentPresenter implements ProcessPaymentOutputBoundary {

    private ProcessPaymentViewModel viewModel;

    public ProcessPaymentPresenter(ProcessPaymentViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(ProcessPaymentResponseData response) {
        viewModel.success = String.valueOf(response.success);
        viewModel.message = response.message;
        
        // Map optional fields (they might be null in ResponseData)
        viewModel.paymentUrl = response.paymentUrl;
        viewModel.transactionRef = response.transactionRef;
    }

    public ProcessPaymentViewModel getModel() {
        return viewModel;
    }
}
