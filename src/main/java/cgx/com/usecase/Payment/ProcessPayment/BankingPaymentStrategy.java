package cgx.com.usecase.Payment.ProcessPayment;

import java.util.UUID;

import cgx.com.Entities.Order;
import cgx.com.Entities.PaymentMethod;

public class BankingPaymentStrategy implements IPaymentStrategy {

    @Override
    public boolean supports(String paymentMethod) {
        return PaymentMethod.BANKING.name().equalsIgnoreCase(paymentMethod);
    }

    @Override
    public PaymentResult process(Order order) {
        // Logic Banking: Tạo mã QR hoặc Link thanh toán.
        // Trong thực tế: Gọi API VietQR hoặc cổng thanh toán.
        // Ở đây giả lập tạo link QR.
        
        String bankAccount = "123456789";
        String bankName = "MBBank";
        String amount = order.getTotalAmount().toPlainString();
        String content = "THANHTOAN " + order.getId();
        
        // Giả lập link QR (ví dụ dùng dịch vụ tạo QR nhanh)
        String qrLink = "https://img.vietqr.io/image/" + bankName + "-" + bankAccount + "-compact2.jpg?amount=" + amount + "&addInfo=" + content;
        
        String transId = "BANK-" + UUID.randomUUID().toString();
        
        return new PaymentResult(true, "Vui lòng quét mã QR để thanh toán.", qrLink, transId);
    }
}