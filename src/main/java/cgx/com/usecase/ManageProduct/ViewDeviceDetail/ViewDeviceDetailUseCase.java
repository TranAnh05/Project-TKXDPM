package cgx.com.usecase.ManageProduct.ViewDeviceDetail;

import cgx.com.Entities.ComputerDevice;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

public class ViewDeviceDetailUseCase implements ViewDeviceDetailInputBoundary {

    private final IDeviceRepository deviceRepository;
    private final IAuthTokenValidator tokenValidator;
    private final ViewDeviceDetailOutputBoundary outputBoundary;

    public ViewDeviceDetailUseCase(IDeviceRepository deviceRepository,
                                   IAuthTokenValidator tokenValidator,
                                   ViewDeviceDetailOutputBoundary outputBoundary) {
        this.deviceRepository = deviceRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(ViewDeviceDetailRequestData input) {
        ViewDeviceDetailResponseData output = new ViewDeviceDetailResponseData();

        try {
        	ComputerDevice.validateId(input.deviceId);

            DeviceData deviceData = deviceRepository.findById(input.deviceId);
            
            if (deviceData == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm.");
            }
            
            output.success = true;
            output.message = "Lấy thông tin sản phẩm thành công.";
            output.device = deviceData;

        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống không xác định.";
        }

        outputBoundary.present(output);
    }
}
