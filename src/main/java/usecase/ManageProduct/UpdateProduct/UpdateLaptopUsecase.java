package usecase.ManageProduct.UpdateProduct;

import java.util.Map;

import Entities.Laptop;
import Entities.Product;
import usecase.ManageProduct.ProductData;

public class UpdateLaptopUsecase extends UpdateProductUsecase{

	@Override
	protected ProductData mapEntityToData(Product entity) {
		Laptop laptop = (Laptop) entity; // Ép kiểu (Cast) an toàn
        
        ProductData data = new ProductData();
        // Gán thuộc tính chung
        data.id = entity.getId(); // Tạo mới
        data.name = laptop.getName();
        data.description = laptop.getDescription();
        data.price = laptop.getPrice();
        data.stockQuantity = laptop.getStockQuantity();
        data.imageUrl = laptop.getImageUrl();
        data.categoryId = laptop.getCategoryId();
        
        // Gán thuộc tính riêng
        data.cpu = laptop.getCpu();
        data.ram = laptop.getRam();
        data.screenSize = laptop.getScreenSize();
        
        return data;
	}

	@Override
	protected Product createEntity(UpdateProductInputData input) {
		Map<String, String> attrs = input.attributes;
		return new Laptop(input.id, input.name, input.description, input.price,
				input.stockQuantity, input.imageUrl, input.categoryId,
				attrs.get("cpu"), 
	            Integer.parseInt(attrs.get("ram")), 
	            attrs.get("screenSize")
		);
	}

	@Override
	protected void validateSpecificAttributes(Map<String, String> attributes) {
		try {
            String cpu = attributes.get("cpu");
            int ram = Integer.parseInt(attributes.get("ram"));
            String screenSize = attributes.get("screenSize");
            // (Gọi Tầng 4 (Entity) để validate)
            Laptop.validateSpecific(cpu, ram, screenSize);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("RAM phải là một con số.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Thuộc tính Laptop không hợp lệ: " + e.getMessage());
        }
	}

}
