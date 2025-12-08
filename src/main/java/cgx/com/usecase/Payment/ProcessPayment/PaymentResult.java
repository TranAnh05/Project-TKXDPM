package cgx.com.usecase.Payment.ProcessPayment;


/**
 * Kết quả trả về sau khi xử lý thanh toán.
 * Dữ liệu này sẽ được gửi về cho Frontend để hiển thị.
 */
public class PaymentResult {
    public boolean isSuccessful;
    public String message;
    public String paymentUrl; // Link thanh toán hoặc mã QR (nếu có)
    public String transactionId; // Mã giao dịch tham chiếu
    
    public PaymentResult(boolean isSuccessful, String message, String paymentUrl, String transactionId) {
        this.isSuccessful = isSuccessful;
        this.message = message;
        this.paymentUrl = paymentUrl;
        this.transactionId = transactionId;
    }
}