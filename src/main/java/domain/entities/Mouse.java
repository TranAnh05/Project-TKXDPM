package domain.entities;

import java.util.Map;

public class Mouse extends Product{
	private String connectionType; // "Wireless" or "Wired"
    private int dpi;
    
	public Mouse(String name, String description, double price, int stockQuantity, String imageUrl, int categoryId,
			String connectionType, int dpi) {
		super(name, description, price, stockQuantity, imageUrl, categoryId);
		this.connectionType = connectionType;
		this.dpi = dpi;
	}
	
	public Mouse(int id, String name, String description, double price, int stockQuantity, String imageUrl, int categoryId,
			String connectionType, int dpi) {
		super(id, name, description, price, stockQuantity, imageUrl, categoryId);
		this.connectionType = connectionType;
		this.dpi = dpi;
	}
	
	public static void validateDpi(int dpi) {
		if (dpi <= 0) {
            throw new IllegalArgumentException("DPI phải là số dương.");
        }
	}
	
	public static void validateConnectionType(String connectType) {
        if (connectType == null || connectType.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại connectionType không được để trống.");
        }
    }
	
	public String getConnectionType() { return connectionType; }
    public int getDpi() { return dpi; }

	@Override
	public void updateSpecific(Map<String, String> attributes) {
		try {
            // 1. Lấy thuộc tính riêng từ Map
            String connectionType = attributes.get("connectionType");
            int dpi = Integer.parseInt(attributes.get("dpi"));
            
            // 2. Validate dữ liệu riêng (T4)
            validateConnectionType(connectionType);
            validateDpi(dpi);
            
            // 3. Gán giá trị
            this.connectionType = connectionType;
            this.dpi = dpi;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("DPI phải là số dương.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Thuộc tính Mouse không hợp lệ: " + e.getMessage());
        }
	}
}
