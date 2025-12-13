package cgx.com.usecase.ManageProduct.SearchDevices;

import java.math.BigDecimal;

public class SearchDevicesRequestData {
	public String authToken; 
    public String keyword;
    public String categoryId;
    public BigDecimal minPrice;
    public BigDecimal maxPrice;
    public int page;
    public int size;
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
