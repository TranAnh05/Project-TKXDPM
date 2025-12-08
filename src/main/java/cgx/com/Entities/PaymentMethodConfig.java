package cgx.com.Entities;

public class PaymentMethodConfig {
    private String code;        // VD: "COD", "BANKING"
    private String displayName; // VD: "Thanh toán khi nhận hàng"
    private String description; // VD: "Kiểm tra hàng rồi thanh toán"
    private String iconUrl;     // VD: "/icons/cod.png"
    private boolean isActive;   // true/false
    private int displayOrder;   // Thứ tự hiển thị (1, 2, 3...)

    public PaymentMethodConfig(String code, String displayName, String description, String iconUrl, boolean isActive, int displayOrder) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
        this.iconUrl = iconUrl;
        this.isActive = isActive;
        this.displayOrder = displayOrder;
    }

    // Getters
    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public boolean isActive() { return isActive; }
    public int getDisplayOrder() { return displayOrder; }
}
