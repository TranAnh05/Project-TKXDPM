package cgx.com.usecase.Payment;

import java.util.List;

import cgx.com.Entities.PaymentMethodConfig;

public interface IPaymentMethodRepository {
	List<PaymentMethodConfig> findAllActive();
}
