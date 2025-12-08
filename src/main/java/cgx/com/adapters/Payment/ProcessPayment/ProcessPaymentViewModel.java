package cgx.com.adapters.Payment.ProcessPayment;

public class ProcessPaymentViewModel {
    public String success;
    public String message;
    public String paymentUrl;      // URL for QR code or redirect (can be null)
    public String transactionRef;  // Reference ID for the transaction
}
