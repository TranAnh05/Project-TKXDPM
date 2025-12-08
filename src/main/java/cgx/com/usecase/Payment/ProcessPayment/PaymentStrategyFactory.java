package cgx.com.usecase.Payment.ProcessPayment;
import java.util.List;

/**
 * Factory để chọn Strategy phù hợp.
 */
public class PaymentStrategyFactory {
    private final List<IPaymentStrategy> strategies;

    public PaymentStrategyFactory(List<IPaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    public IPaymentStrategy getStrategy(String paymentMethod) {
        return strategies.stream()
                .filter(s -> s.supports(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ: " + paymentMethod));
    }
}