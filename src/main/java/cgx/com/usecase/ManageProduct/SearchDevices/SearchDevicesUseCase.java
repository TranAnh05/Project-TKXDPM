package cgx.com.usecase.ManageProduct.SearchDevices;

import java.math.BigDecimal;
import java.util.List;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;
import cgx.com.usecase.ManageUser.SearchUsers.PaginationData;

public class SearchDevicesUseCase implements SearchDevicesInputBoundary {

    private final IDeviceRepository deviceRepository;
    private final IAuthTokenValidator tokenValidator;
    private final SearchDevicesOutputBoundary outputBoundary;

    public SearchDevicesUseCase(IDeviceRepository deviceRepository,
                                IAuthTokenValidator tokenValidator,
                                SearchDevicesOutputBoundary outputBoundary) {
        this.deviceRepository = deviceRepository;
        this.tokenValidator = tokenValidator;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(SearchDevicesRequestData input) {
    	SearchDevicesResponseData output = new SearchDevicesResponseData();

        try {
            // 1. Validate Logic Giá (MỚI)
            validatePriceRange(input.minPrice, input.maxPrice);

            // 2. Xác định quyền hạn & Status cần lọc
            String targetStatus = "ACTIVE"; 

            if (input.authToken != null && !input.authToken.trim().isEmpty()) {
                try {
                    AuthPrincipal principal = tokenValidator.validate(input.authToken);
                    if (principal.role == UserRole.ADMIN) {
                        targetStatus = input.statusFilter; 
                    }
                } catch (Exception e) {
                    // Token lỗi -> Coi như Guest
                }
            }

            // 3. Xây dựng Criteria
            DeviceSearchCriteria criteria = new DeviceSearchCriteria(
                input.keyword,
                input.categoryId,
                input.minPrice,
                input.maxPrice,
                targetStatus
            );

            // 4. Tìm kiếm (Phân trang)
            int pageIndex = input.page - 1; 
            List<DeviceData> results = deviceRepository.search(criteria, pageIndex, input.size);

            // 5. Đếm tổng số
            long totalCount = deviceRepository.count(criteria);

            // 6. Tạo Pagination Data
            PaginationData pagination = new PaginationData(totalCount, input.page, input.size);

            // 7. Thành công
            output.success = true;
            output.message = "Tìm kiếm thành công.";
            output.devices = results;
            output.pagination = pagination;

        } catch (IllegalArgumentException e) {
            // Bắt lỗi Validation
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống không xác định.";
        }

        outputBoundary.present(output);
    }

    /**
     * Hàm helper để validate khoảng giá.
     */
    private void validatePriceRange(BigDecimal min, BigDecimal max) {
        if (min != null && min.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá thấp nhất không được âm.");
        }
        if (max != null && max.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá cao nhất không được âm.");
        }
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Giá thấp nhất không được lớn hơn giá cao nhất.");
        }
    }
}
