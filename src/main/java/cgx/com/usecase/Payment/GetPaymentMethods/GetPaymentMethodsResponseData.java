package cgx.com.usecase.Payment.GetPaymentMethods;

import java.util.List;

public class GetPaymentMethodsResponseData {
	public boolean success;
    public String message;
    public List<PaymentMethodDTO> methods;
}
