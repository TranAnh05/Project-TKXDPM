package usecase.ManageProduct.UpdateProduct;

import java.util.Map;

import Entities.Keyboard;
import Entities.Product;
import usecase.ManageProduct.ProductData;

public class UpdateKeyboardUsecase extends UpdateProductUsecase{

	@Override
	protected ProductData mapEntityToData(Product entity) {
		Keyboard keyboard = (Keyboard) entity; // Ép kiểu (Cast) an toàn
        
        ProductData data = new ProductData();
        // Gán thuộc tính chung
        data.id = entity.getId();
        data.name = keyboard.getName();
        data.description = keyboard.getDescription();
        data.price = keyboard.getPrice();
        data.stockQuantity = keyboard.getStockQuantity();
        data.imageUrl = keyboard.getImageUrl();
        data.categoryId = keyboard.getCategoryId();
        
        // Gán thuộc tính riêng
        data.switchType = keyboard.getSwitchType();
        data.layout = keyboard.getLayout();
        
        return data;
	}

	@Override
	protected Product createEntity(UpdateProductInputData input) {
		Map<String, String> attrs = input.attributes;
        // (Gọi constructor "sạch" của Tầng 4)
        return new Keyboard(
            input.id, input.name, input.description, input.price, input.stockQuantity, 
            input.imageUrl, input.categoryId,
            attrs.get("switchType"), 
            attrs.get("layout")
        );
	}

	@Override
	protected void validateSpecificAttributes(Map<String, String> attributes) {
		try {
            String switchType = attributes.get("switchType");
            String layout = attributes.get("layout");
            // (Gọi Tầng 4 (Entity) để validate)
            Keyboard.validateSpecific(switchType, layout);
        } catch (Exception e) {
            throw new IllegalArgumentException("Thuộc tính Keyboard không hợp lệ: " + e.getMessage());
        }
	}

}
