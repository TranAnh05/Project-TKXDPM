package cgx.com.usecase.Payment.ProcessPayment;

import java.util.UUID;

import cgx.com.Entities.Order;
import cgx.com.Entities.PaymentMethod;

public class CodPaymentStrategy implements IPaymentStrategy {

    @Override
    public boolean supports(String paymentMethod) {
        return PaymentMethod.COD.name().equalsIgnoreCase(paymentMethod);
    }

    @Override
    public PaymentResult process(Order order) {
        // Logic COD: Đơn giản là xác nhận sẽ thu tiền sau.
        // Tạo mã giao dịch giả lập
        String transId = "COD-" + UUID.randomUUID().toString();
        return new PaymentResult(true, "Vui lòng chuẩn bị tiền mặt khi nhận hàng.", null, transId);
    }
}