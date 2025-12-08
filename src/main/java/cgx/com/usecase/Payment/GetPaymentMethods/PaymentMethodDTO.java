package cgx.com.usecase.Payment.GetPaymentMethods;

public class PaymentMethodDTO {
	public String code;
    public String name;
    public String description;
    public String icon;

    public PaymentMethodDTO(String code, String name, String description, String icon) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }
}	
