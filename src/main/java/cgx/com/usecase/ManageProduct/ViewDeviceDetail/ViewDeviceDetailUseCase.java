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
            // 1. Validate Input
        	ComputerDevice.validateId(input.deviceId);

            // 2. Tìm sản phẩm
            DeviceData deviceData = deviceRepository.findById(input.deviceId);
            
            // 3. Kiểm tra tồn tại
            if (deviceData == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm.");
            }

            // 4. Kiểm tra quyền xem (Business Rule quan trọng)
            // Mặc định: Chỉ xem được hàng ACTIVE
            boolean canView = "ACTIVE".equals(deviceData.status);

            // Nếu không phải ACTIVE, kiểm tra xem có phải Admin không
            if (!canView) {
                if (input.authToken != null && !input.authToken.isEmpty()) {
                    try {
                        AuthPrincipal principal = tokenValidator.validate(input.authToken);
                        if (principal.role == UserRole.ADMIN) {
                            canView = true; // Admin được xem mọi trạng thái
                        }
                    } catch (Exception e) {
                        // Token lỗi hoặc hết hạn -> Coi như Guest -> Không được xem
                    }
                }
            }

            if (!canView) {
                // Ẩn sản phẩm không Active với người thường
                throw new IllegalArgumentException("Không tìm thấy sản phẩm."); 
            }
            
            // 5. Thành công
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
