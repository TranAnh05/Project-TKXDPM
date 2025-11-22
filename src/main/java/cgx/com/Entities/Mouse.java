package cgx.com.Entities;

import java.math.BigDecimal;
import java.time.Instant;import org.hibernate.metamodel.mapping.internal.AbstractStateArrayContributorMapping;

/**
 * Lớp Con: Chuột máy tính.
 * Thuộc tính riêng: DPI, Kết nối, Số nút.
 */
public class Mouse extends ComputerDevice {
    private int dpi;
    private boolean isWireless;
    private int buttonCount;

    public Mouse(String id, String name, String description, BigDecimal price, int stockQuantity, 
                 String categoryId, String status, String thumbnail, Instant createdAt, Instant updatedAt,
                 int dpi, boolean isWireless, int buttonCount) {
        super(id, name, description, price, stockQuantity, categoryId, status, thumbnail, createdAt, updatedAt);
        this.dpi = dpi;
        this.isWireless = isWireless;
        this.buttonCount = buttonCount;
    }

    public static void validateSpecs(int dpi, int buttonCount) {
        if (dpi <= 0) throw new IllegalArgumentException("DPI phải lớn hơn 0.");
        if(buttonCount <= 0) throw new IllegalArgumentException("Button Count phải lớn hơn 0.");
    }
    
    // Getters...
    public int getDpi() { return dpi; }
    public boolean isWireless() { return isWireless; }
    public int getButtonCount() { return buttonCount; }
}
