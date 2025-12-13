package cgx.com.usecase.ManageProduct.SearchDevices;

import java.math.BigDecimal;
import java.util.List;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.Interface_Common.PaginationData;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.IDeviceRepository;

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
            validatePriceRange(input.minPrice, input.maxPrice);

            String targetStatus = "AVAILABLE"; 

            if (input.authToken != null && !input.authToken.trim().isEmpty()) {
                AuthPrincipal principal = tokenValidator.validate(input.authToken);
                if (principal.role == UserRole.ADMIN && input.statusFilter != null && !input.statusFilter.isEmpty()) {
                    targetStatus = input.statusFilter;
                }
            }

            DeviceSearchCriteria criteria = new DeviceSearchCriteria(
                input.keyword,
                input.categoryId,
                input.minPrice,
                input.maxPrice,
                targetStatus
            );

            // Tìm kiếm (Phân trang)
            int pageIndex = input.page - 1; 
            List<DeviceData> results = deviceRepository.search(criteria, pageIndex, input.size);

            long totalCount = deviceRepository.count(criteria);

            PaginationData pagination = new PaginationData(totalCount, input.page, input.size);

            output.success = true;
            output.message = "Tìm kiếm thành công.";
            output.devices = results;
            output.pagination = pagination;

        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống không xác định.";
            e.printStackTrace();        }

        outputBoundary.present(output);
    }

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
