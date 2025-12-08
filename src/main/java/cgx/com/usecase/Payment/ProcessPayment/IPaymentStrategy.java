package cgx.com.usecase.Payment.ProcessPayment;

import cgx.com.Entities.Order;

public interface IPaymentStrategy {
	boolean supports(String paymentMethod);
    PaymentResult process(Order order);
}
