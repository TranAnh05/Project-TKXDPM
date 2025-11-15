package usecase.ManageProduct.AddNewProduct;

import java.util.Map;

import Entities.Laptop;
import Entities.Mouse;
import Entities.Product;
import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageProduct.ProductData;

public class AddNewMouseUsecase extends AddNewProducUsecase{

	public AddNewMouseUsecase(ProductRepository productRepository, CategoryRepository categoryRepository,
			AddNewProductOutputBoundary outBoundary) {
		super(productRepository, categoryRepository, outBoundary);
	}

	@Override
	protected ProductData mapEntityToData(Product entity) {
		Mouse mouse = (Mouse) entity; // Ép kiểu (Cast) an toàn
        
        ProductData data = new ProductData();
        // Gán thuộc tính chung
        data.id = 0; // Tạo mới
        data.name = mouse.getName();
        data.description = mouse.getDescription();
        data.price = mouse.getPrice();
        data.stockQuantity = mouse.getStockQuantity();
        data.imageUrl = mouse.getImageUrl();
        data.categoryId = mouse.getCategoryId();
        
        // Gán thuộc tính riêng
        data.connectionType = mouse.getConnectionType();
        data.dpi = mouse.getDpi();
        
        return data;
	}

	@Override
	protected Product createEntity(AddNewProductInputData input) {
		Map<String, String> attrs = input.attributes;
        // (Gọi constructor "sạch" của Tầng 4)
        return new Mouse(
            input.name, input.description, input.price, input.stockQuantity, 
            input.imageUrl, input.categoryId,
            attrs.get("connectionType"), 
            Integer.parseInt(attrs.get("dpi"))
        );
	}

	@Override
	protected void validateSpecificAttributes(Map<String, String> attributes) {
		try {
            int dpi = Integer.parseInt(attributes.get("dpi"));
            String connectionType = attributes.get("connectionType");
            // (Gọi Tầng 4 (Entity) để validate)
            Mouse.validateSpecific(dpi, connectionType);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("DPI phải là một con số.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Thuộc tính Mouse không hợp lệ: " + e.getMessage());
        }
	}

}
