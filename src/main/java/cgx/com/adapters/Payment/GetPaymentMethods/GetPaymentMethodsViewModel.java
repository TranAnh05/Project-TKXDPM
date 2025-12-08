package cgx.com.adapters.Payment.GetPaymentMethods;

import java.util.List;

public class GetPaymentMethodsViewModel {
	public String isSuccess; // String "true"/"false" để dễ bind lên UI nếu cần
    public String errorMessage;
    public List<PaymentMethodViewDTO> methodList;
}
