package usecase.ManageProduct.AddNewProduct;

import java.util.Map;

import Entities.Product;
import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageProduct.ProductData;
import usecase.ManageProduct.ProductOutputData;

public abstract class AddNewProducUsecase implements AddNewProductInputBoundary{
	protected ProductRepository productRepository;
	protected CategoryRepository categoryRepository;
	protected AddNewProductOutputBoundary outBoundary;;
	
	protected AddNewProductOutputData outputData;

	public AddNewProducUsecase(ProductRepository productRepository, CategoryRepository categoryRepository,
			AddNewProductOutputBoundary outBoundary) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.outBoundary = outBoundary;
	}
	
	public AddNewProductOutputData getOutputData() {
		return outputData;
	}
	
	public void execute(AddNewProductInputData input) {
		outputData = new AddNewProductOutputData();
		
		try {
			// 1. Validate chung
			Product.validateCommon(input.name, input.description, input.price, input.stockQuantity);
			
			// 2. GỌI VALIDATION (Tầng 4 - Riêng)
            // (Hàm này là 'abstract' - lớp Con sẽ tự định nghĩa)
            validateSpecificAttributes(input.attributes);
            
            // 3. Kiểm tra nghiệp vụ (Tầng 3 - Chung)
            if (productRepository.findByName(input.name) != null) {
                throw new IllegalArgumentException("Tên sản phẩm này đã tồn tại.");
            }
            if (categoryRepository.findById(input.categoryId) == null) {
                 throw new IllegalArgumentException("Loại sản phẩm không hợp lệ.");
            }
            
            // 4.TẠO ENTITY (Tầng 4 - Riêng)
            Product productEntity = createEntity(input);
            
            // 5. CHUYỂN (MAP) (Tầng 4 -> T3 DTO - Riêng)
            // (Hàm này là 'abstract' - lớp Con tự "làm phẳng" (flatten))
            ProductData dataToSave = mapEntityToData(productEntity);
            
            // 6. LƯU VÀO CSDL (Tầng 3 - Chung)
            ProductData savedData = productRepository.save(dataToSave);
            
            // 7. Báo cáo thành công (Chung)
            outputData.success = true;
            outputData.message = "Thêm mới thành công!";
            outputData.newProduct = mapDataToOutput(savedData); // (Dùng hàm helper chung)
		} catch (IllegalArgumentException e) {
            // 9. BẮT LỖI VALIDATION (T4) HOẶC LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
		} catch (Exception e) {
            // 10. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }
		
		outBoundary.present(outputData);
	}

	private ProductOutputData mapDataToOutput(ProductData data) {
		ProductOutputData dto = new ProductOutputData();
		dto.id = data.id;
        dto.name = data.name;
        dto.description = data.description;
        dto.price = data.price;
        dto.stockQuantity = data.stockQuantity;
        dto.imageUrl = data.imageUrl;
        dto.categoryId = data.categoryId;
        
        // Chép các thuộc tính riêng
        dto.cpu = data.cpu;
        dto.ram = data.ram;
        dto.screenSize = data.screenSize;
        dto.connectionType = data.connectionType;
        dto.dpi = data.dpi;
        dto.switchType = data.switchType;
        dto.layout = data.layout;
        return dto;
	}

	protected abstract ProductData mapEntityToData(Product productEntity);

	protected abstract Product createEntity(AddNewProductInputData input);

	protected abstract void validateSpecificAttributes(Map<String, String> attributes);
}
