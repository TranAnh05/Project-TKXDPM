package cgx.com.adapters.Payment.GetPaymentMethods;

import java.util.ArrayList;
import java.util.stream.Collectors;

import cgx.com.usecase.Payment.GetPaymentMethods.GetPaymentMethodsOutputBoundary;
import cgx.com.usecase.Payment.GetPaymentMethods.GetPaymentMethodsResponseData;
import cgx.com.usecase.Payment.GetPaymentMethods.PaymentMethodDTO;

public class GetPaymentMethodsPresenter implements GetPaymentMethodsOutputBoundary {

    private GetPaymentMethodsViewModel viewModel;

    public GetPaymentMethodsPresenter(GetPaymentMethodsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(GetPaymentMethodsResponseData response) {
        viewModel.isSuccess = String.valueOf(response.success);
        viewModel.errorMessage = response.message;

        if (response.success && response.methods != null) {
            // Map từ UseCase DTO sang View DTO
            viewModel.methodList = response.methods.stream()
                .map(this::mapToViewDTO)
                .collect(Collectors.toList());
        } else {
            viewModel.methodList = new ArrayList<>();
        }
    }

    private PaymentMethodViewDTO mapToViewDTO(PaymentMethodDTO dto) {
        PaymentMethodViewDTO viewDto = new PaymentMethodViewDTO();
        viewDto.code = dto.code;
        viewDto.label = dto.name;
        viewDto.subLabel = dto.description;
        // Có thể xử lý thêm logic đường dẫn ảnh ở đây (ví dụ thêm domain)
        viewDto.imageSource = dto.icon; 
        return viewDto;
    }
    
    public GetPaymentMethodsViewModel getModel() {
        return this.viewModel;
    }
}