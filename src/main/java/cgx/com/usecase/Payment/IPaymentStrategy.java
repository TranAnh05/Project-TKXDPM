package cgx.com.usecase.Payment;

import cgx.com.Entities.Order;

public interface IPaymentStrategy {
	boolean supports(String paymentMethod);
    PaymentResult process(Order order);
}
