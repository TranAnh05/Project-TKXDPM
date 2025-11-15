package Entities;

import java.util.HashMap;
import java.util.Map;

public class Laptop extends Product{
	// Thuộc tính riêng
    private String cpu;
    private int ram;
    private String screenSize;
    
	public Laptop(String name, String description, double price, int stockQuantity, String imageUrl, int categoryId,
			String cpu, int ram, String screenSize) {
		super(name, description, price, stockQuantity, imageUrl, categoryId);
		this.cpu = cpu;
		this.ram = ram;
		this.screenSize = screenSize;
	}

	public Laptop(int id, String name, String description, double price, int stockQuantity, String imageUrl, int categoryId,
			String cpu, int ram, String screenSize) {
		super(id, name, description, price, stockQuantity, imageUrl, categoryId);
		this.cpu = cpu;
		this.ram = ram;
		this.screenSize = screenSize;
	}
	
	public static void validateSpecific(String cpu, int ram, String screenSize) {
		if (cpu == null || cpu.trim().isEmpty()) {
            throw new IllegalArgumentException("CPU không được để trống.");
        }
		
		if (ram <= 0) {
            throw new IllegalArgumentException("RAM phải là số dương.");
        }
		
		 if (screenSize == null || screenSize.trim().isEmpty()) {
	            throw new IllegalArgumentException("Kích thước màn hình không được để trống.");
	     }
	}
	
	public String getCpu() { return cpu; }
    public int getRam() { return ram; }
    public String getScreenSize() { return screenSize; }

	@Override
	public void updateSpecific(Map<String, String> attributes) {
		try {
            // 1. Lấy thuộc tính riêng từ Map
            String cpu = attributes.get("cpu");
            int ram = Integer.parseInt(attributes.get("ram"));
            String screenSize = attributes.get("screenSize");
            
            // 2. Validate dữ liệu riêng (T4)
            validateSpecific(cpu, ram, screenSize);
            
            // 3. Gán giá trị
            this.cpu = cpu;
            this.ram = ram;
            this.screenSize = screenSize;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("RAM phải là một con số.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Thuộc tính Laptop không hợp lệ: " + e.getMessage());
        }
	}

	@Override
	public Map<String, String> getSpecificAttributes() {
		Map<String, String> specifics = new HashMap<>();
        specifics.put("cpu", this.cpu);
        specifics.put("ram", String.valueOf(this.ram));
        specifics.put("screenSize", this.screenSize);
        return specifics;
	}
}
