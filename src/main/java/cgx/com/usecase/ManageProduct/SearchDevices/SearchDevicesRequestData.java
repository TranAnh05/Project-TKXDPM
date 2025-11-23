package cgx.com.usecase.ManageProduct.SearchDevices;

import java.math.BigDecimal;

public class SearchDevicesRequestData {
	public final String authToken; // Để xác định quyền (Admin thấy hết, Khách chỉ thấy Active)
    public final String keyword;
    public final String categoryId;
    public final BigDecimal minPrice;
    public final BigDecimal maxPrice;
    public final int page;
    public final int size;
    
    // Admin có thể muốn lọc theo status cụ thể (ví dụ: tìm hàng OUT_OF_STOCK)
    public final String statusFilter; 

    public SearchDevicesRequestData(String authToken, String keyword, String categoryId, 
                                    BigDecimal minPrice, BigDecimal maxPrice, 
                                    String statusFilter, int page, int size) {
        this.authToken = authToken;
        this.keyword = keyword;
        this.categoryId = categoryId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.statusFilter = statusFilter;
        this.page = (page <= 0) ? 1 : page;
        this.size = (size <= 0) ? 10 : size;
    }
}
