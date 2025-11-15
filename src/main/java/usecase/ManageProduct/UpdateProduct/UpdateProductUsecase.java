package usecase.ManageProduct.UpdateProduct;

import java.util.Map;

import Entities.Product;
import application.factories.ManageProduct.ProductFactory;
import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageProduct.ProductData;
import usecase.ManageProduct.ProductOutputData;

public abstract class UpdateProductUsecase implements UpdateProductInputBoundary{
	private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private UpdateProductOutputBoundary productPresenter;
    
    private UpdateProductOutputData outputData; 
    
    public UpdateProductUsecase() {
    	
    }
	
	public UpdateProductUsecase(ProductRepository productRepository, CategoryRepository categoryRepository,
			UpdateProductOutputBoundary productPresenter) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.productPresenter = productPresenter;
	}
	
	public UpdateProductOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(UpdateProductInputData input) {
		outputData = new UpdateProductOutputData();
		
		try {
			// 1. Validate dữ liệu
			Product.validateCommon(input.name, input.description, input.price, input.stockQuantity);
			
			// 2. Validate riêng
			validateSpecificAttributes(input.attributes);
			
			// 3. Kiểm tra id
			ProductData existingProduct = productRepository.findById(input.id);
            if (existingProduct == null) {
            	outputData.success = false;
            	outputData.message = "Không tìm thấy sản phẩm để cập nhật.";
            	productPresenter.present(outputData);
                return;
            }  
            
            // 4. Kiểm tra nghiệp vụ (Tên trùng)
            ProductData productWithSameName = productRepository.findByName(input.name);
            if (productWithSameName != null && productWithSameName.id != input.id) {
            	outputData.success = false;
            	outputData.message = "Tên sản phẩm này đã tồn tại.";
            	productPresenter.present(outputData);
                return;
            }
            
            CategoryData categoryData = categoryRepository.findById(input.categoryId);
            
            // 5. Cập nhật sản phẩm
            Product udpatedProduct = createEntity(input);
            
            // 6. Chuyển entity sang dto để lưu vào csdl
            ProductData dataToSave = mapEntityToData(udpatedProduct);
            
            // 7. Lưu vào csdl
            ProductData savedData = productRepository.update(dataToSave);
            
            // 8. Báo cáo thành công
            outputData.success = true;
        	outputData.message = "Cập nhật sản phẩm thành công.";
        	outputData.updatedProduct = mapDataToOutput(savedData, categoryData);
            
			
		} catch (IllegalArgumentException e) {
            // 9. BẮT LỖI VALIDATION (T4) HOẶC LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
		} catch (Exception e) {
            // 10. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }
		
		productPresenter.present(outputData);
	}

	private ProductOutputData mapDataToOutput(ProductData data, CategoryData category) {
		ProductOutputData dto = new ProductOutputData();
		dto.id = data.id;
        dto.name = data.name;
        dto.description = data.description;
        dto.price = data.price;
        dto.stockQuantity = data.stockQuantity;
        dto.imageUrl = data.imageUrl;
        dto.categoryId = data.categoryId;
        dto.categoryName = category.name;
        
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

	protected abstract ProductData mapEntityToData(Product udpatedProduct);

	protected abstract Product createEntity(UpdateProductInputData input);

	protected abstract void validateSpecificAttributes(Map<String, String> attributes);

}
