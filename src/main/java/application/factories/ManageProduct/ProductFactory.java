package application.factories.ManageProduct;

import Entities.Keyboard;
import Entities.Laptop;
import Entities.Mouse;
import Entities.Product;
import usecase.ManageCategory.CategoryData;
import usecase.ManageProduct.ProductData;

public class ProductFactory {
	public Product load(ProductData data, CategoryData category) {
        if (data == null || category == null) {
            throw new IllegalArgumentException("Dữ liệu sản phẩm hoặc loại không hợp lệ để tải.");
        }
        
        if (category.name == null) {
            throw new IllegalArgumentException("Tên loại sản phẩm bị trống.");
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
            	throw new IllegalArgumentException("Loại sản phẩm không được hỗ trợ: " + category.name);
        }
    }
}
