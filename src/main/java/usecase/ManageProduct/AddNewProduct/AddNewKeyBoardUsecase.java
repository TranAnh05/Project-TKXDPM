package usecase.ManageProduct.AddNewProduct;

import java.util.Map;

import Entities.Keyboard;
import Entities.Mouse;
import Entities.Product;
import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageProduct.ProductData;

public class AddNewKeyBoardUsecase extends AddNewProducUsecase{

	public AddNewKeyBoardUsecase(ProductRepository productRepository, CategoryRepository categoryRepository,
			AddNewProductOutputBoundary outBoundary) {
		super(productRepository, categoryRepository, outBoundary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ProductData mapEntityToData(Product entity) {
		Keyboard keyboard = (Keyboard) entity; // Ép kiểu (Cast) an toàn
        
        ProductData data = new ProductData();
        // Gán thuộc tính chung
        data.id = 0; // Tạo mới
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
	protected Product createEntity(AddNewProductInputData input) {
		Map<String, String> attrs = input.attributes;
        // (Gọi constructor "sạch" của Tầng 4)
        return new Keyboard(
            input.name, input.description, input.price, input.stockQuantity, 
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
