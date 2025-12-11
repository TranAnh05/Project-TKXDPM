package cgx.com.usecase.Payment;

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
        // Tạo mã giao dịch giả lập
        return new PaymentResult(true, "Vui lòng chuẩn bị tiền mặt khi nhận hàng.", null, null);
    }
}