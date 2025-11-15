package Entities;

import java.util.HashMap;
import java.util.Map;

public class Keyboard extends Product{
	private String switchType; // Ví dụ: "Blue", "Red", "Brown"
    private String layout; // Ví dụ: "Full-size", "Tenkeyless"
    
	public Keyboard(String name, String description, double price, int stockQuantity, String imageUrl, int categoryId,
			String switchType, String layout) {
		super(name, description, price, stockQuantity, imageUrl, categoryId);
		this.switchType = switchType;
		this.layout = layout;
	}
	
	public Keyboard(int id, String name, String description, double price, int stockQuantity, String imageUrl, int categoryId,
			String switchType, String layout) {
		super(id, name, description, price, stockQuantity, imageUrl, categoryId);
		this.switchType = switchType;
		this.layout = layout;
	}
	
	public static void validateSpecific(String switchType, String layout) {
		if (switchType == null || switchType.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại switch không được để trống.");
        }
		
		if (layout == null || layout.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại layout không được để trống.");
        }
	}
	
	public String getSwitchType() { return switchType; }
    public String getLayout() { return layout; }

	@Override
	public void updateSpecific(Map<String, String> attributes) {
		try {
            // 1. Lấy thuộc tính riêng từ Map
            String switchType = attributes.get("switchType");
            String layout = attributes.get("layout");
            
            // 2. Validate dữ liệu riêng (T4)
            validateSpecific(switchType, layout);
            
            // 3. Gán giá trị
            this.switchType = switchType;
            this.layout = layout;
        } catch (Exception e) {
            throw new IllegalArgumentException("Thuộc tính Keyboard không hợp lệ: " + e.getMessage());
        }
	}
    
	@Override
	public Map<String, String> getSpecificAttributes() {
		Map<String, String> specifics = new HashMap<>();
        specifics.put("switchType", this.switchType);
        specifics.put("layout", this.layout);
        return specifics;
	}
    
}
