package cgx.com.Entities;

public enum ProductAvailability {
	AVAILABLE,          // Còn hàng
    OUT_OF_STOCK,       // Hết hàng (Stock = 0)
    NOT_ENOUGH_STOCK,   // Không đủ số lượng (Stock < Yêu cầu)
    DISCONTINUED        // Ngừng kinh doanh (Status != ACTIVE)
}
