package cgx.com.usecase.Payment.ProcessPayment;

public class ProcessPaymentRequestData {
	public final String authToken;
    public final String orderId;
    public final String selectedPaymentMethod; 

    public ProcessPaymentRequestData(String authToken, String orderId, String selectedPaymentMethod) {
        this.authToken = authToken;
        this.orderId = orderId;
        this.selectedPaymentMethod = selectedPaymentMethod;
    }
}
