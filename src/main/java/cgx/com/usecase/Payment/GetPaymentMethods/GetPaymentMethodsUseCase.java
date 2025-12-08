package cgx.com.usecase.Payment.GetPaymentMethods;

import java.util.List;
import java.util.stream.Collectors;

import cgx.com.Entities.PaymentMethodConfig;
import cgx.com.usecase.Payment.IPaymentMethodRepository;

import java.util.ArrayList;

public class GetPaymentMethodsUseCase implements GetPaymentMethodsInputBoundary {

    private final IPaymentMethodRepository repository;
    private final GetPaymentMethodsOutputBoundary outputBoundary;

    public GetPaymentMethodsUseCase(IPaymentMethodRepository repository, 
                                    GetPaymentMethodsOutputBoundary outputBoundary) {
        this.repository = repository;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(GetPaymentMethodsRequestData input) {
        GetPaymentMethodsResponseData output = new GetPaymentMethodsResponseData();

        try {
            // 1. Lấy dữ liệu từ DB (Repository)
            List<PaymentMethodConfig> entities = repository.findAllActive();

            // 2. Chuyển đổi Entity -> DTO
            if (entities != null) {
                output.methods = entities.stream()
                    .map(e -> new PaymentMethodDTO(
                        e.getCode(), 
                        e.getDisplayName(), 
                        e.getDescription(), 
                        e.getIconUrl()
                    ))
                    .collect(Collectors.toList());
            } else {
                output.methods = new ArrayList<>();
            }

            // 3. Success
            output.success = true;
            output.message = "Lấy danh sách phương thức thanh toán thành công.";

        } catch (Exception e) {
            // 4. Fail
            output.success = false;
            output.message = "Lỗi hệ thống: " + e.getMessage();
            output.methods = new ArrayList<>();
        }

        // 5. Gửi ra Presenter
        outputBoundary.present(output);
    }
}