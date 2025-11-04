package application.factories.ManageProduct;

import java.util.Map;

import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageProduct.ProductData;
import domain.entities.Keyboard;
import domain.entities.Laptop;
import domain.entities.Mouse;
import domain.entities.Product;

public class ProductFactory {
	public Product create(String name, String description, double price, int stock, String img, 
            CategoryData category, Map<String, String> attributes) 
		{
		// 1. Validate dữ liệu 
		Product.validateName(name);
		Product.validatePrice(price);
		Product.validateStock(stock);
		Product.validateDescription(description);
		
		// 2. Logic "switch"
		switch (category.name.toUpperCase()) {
		case "LAPTOP":
		  return createLaptop(name, description, price, stock, img, category.id, attributes);
		
		case "MOUSE":
		  return createMouse(name, description, price, stock, img, category.id, attributes);
		  
		case "KEYBOARD":
		  return createKeyboard(name, description, price, stock, img, category.id, attributes);
		  
		default:
		  throw new IllegalArgumentException("Loại sản phẩm không được hỗ trợ: " + category.name);
		}
	}
	
	private Laptop createLaptop(String name, String desc, double price, int stock, String img, 
            int categoryId, Map<String, String> attributes) {
		try {
			// Lấy thuộc tính riêng
			String cpu = attributes.get("cpu");
			int ram = Integer.parseInt(attributes.get("ram"));
			String screenSize = attributes.get("screenSize");
			
			// 2. Validate dữ liệu riêng (T4)
			Laptop.validateCpu(cpu);
			Laptop.validateRam(ram);
			Laptop.validateScreenSize(screenSize);
			
			// 3. Gọi constructor "sạch" (T4)
			return new Laptop(name, desc, price, stock, img, categoryId, cpu, ram, screenSize);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("RAM phải là một con số.");
		} catch (Exception e) {
			throw new IllegalArgumentException("Thuộc tính Laptop không hợp lệ: " + e.getMessage());
		}
	}
	
	// Hàm helper tạo Mouse
    private Mouse createMouse(String name, String desc, double price, int stock, String img, 
                              int categoryId, Map<String, String> attributes) {
        try {
            String connectionType = attributes.get("connectionType");
            int dpi = Integer.parseInt(attributes.get("dpi"));
            
            Mouse.validateDpi(dpi);
            Mouse.validateConnectionType(connectionType);
            
            return new Mouse(name, desc, price, stock, img, categoryId, connectionType, dpi);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("DPI phải là một con số.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Thuộc tính Chuột không hợp lệ: " + e.getMessage());
        }
    }
	
	private Keyboard createKeyboard(String name, String desc, double price, int stock, String img, 
            int categoryId, Map<String, String> attributes) {
		try {
			String switchType = attributes.get("switchType");
			String layout = attributes.get("layout");
			
			// 2. Validate dữ liệu riêng (T4)
			Keyboard.validateSwitchType(switchType);
			Keyboard.validateLayout(layout);
			
			// 3. Gọi constructor "sạch" (T4)
			return new Keyboard(name, desc, price, stock, img, categoryId, switchType, layout);
		} catch (Exception e) {
			throw new IllegalArgumentException("Thuộc tính Bàn phím không hợp lệ: " + e.getMessage());
		}
	}
	
	public Product load(ProductData data, CategoryData category) {
        if (data == null || category == null) {
            throw new IllegalArgumentException("Dữ liệu sản phẩm hoặc loại không hợp lệ.");
        }
        
        // Dùng tên của Category để quyết định tạo Entity nào
        switch (category.name.toUpperCase()) {
            case "LAPTOP":
                return new Laptop(data.id, data.name, data.description, data.price, 
                                  data.stockQuantity, data.imageUrl, data.categoryId,
                                  data.cpu, data.ram, data.screenSize);
            
            case "MOUSE":
                return new Mouse(data.id, data.name, data.description, data.price,
                                 data.stockQuantity, data.imageUrl, data.categoryId,
                                 data.connectionType, data.dpi);
                                 
            case "KEYBOARD":
                return new Keyboard(data.id, data.name, data.description, data.price,
                                    data.stockQuantity, data.imageUrl, data.categoryId,
                                    data.switchType, data.layout);
            
            default:
                // Nếu là loại SP không (còn) hỗ trợ kế thừa, có thể tạo 1 Product "chung"
                // (Điều này đòi hỏi T4 phải có 1 lớp con Product "chung")
                // Hoặc ném lỗi
                throw new IllegalArgumentException("Không thể tải loại sản phẩm: " + category.name);
        }
	}   
}
