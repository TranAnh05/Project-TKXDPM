package cgx.com.usecase.ManageProduct.SearchDevices;

import java.math.BigDecimal;

/**
 * DTO chứa các tiêu chí lọc sản phẩm.
 */
public class DeviceSearchCriteria {
    public final String keyword;      // Tìm theo tên hoặc mô tả
    public final String categoryId;   // Lọc theo danh mục
    public final BigDecimal minPrice; // Giá thấp nhất
    public final BigDecimal maxPrice; // Giá cao nhất
    public final String status;       // Lọc theo trạng thái (ACTIVE, INACTIVE...)

    public DeviceSearchCriteria(String keyword, String categoryId, BigDecimal minPrice, BigDecimal maxPrice, String status) {
        this.keyword = keyword;
        this.categoryId = categoryId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.status = status;
    }
}
