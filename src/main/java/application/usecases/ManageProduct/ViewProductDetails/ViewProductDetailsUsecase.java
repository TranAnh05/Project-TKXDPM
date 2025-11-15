package application.usecases.ManageProduct.ViewProductDetails;

import java.util.Map;

import Entities.Product;
import application.factories.ManageProduct.ProductFactory;
import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageProduct.ProductData;
import usecase.ManageProduct.ProductOutputData;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsInputBoundary;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsInputData;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsOutputBoundary;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsOutputData;

public class ViewProductDetailsUsecase implements ViewProductDetailsInputBoundary{
	private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private ViewProductDetailsOutputBoundary productPresenter;
    private ProductFactory productFactory;
    
    private ViewProductDetailsOutputData outputData; // Field cho TDD
    
    public ViewProductDetailsUsecase() {
    	
    }
    
	public ViewProductDetailsUsecase(ProductRepository productRepository, CategoryRepository categoryRepository,
			ViewProductDetailsOutputBoundary productPresenter, ProductFactory productFactory) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.productPresenter = productPresenter;
		this.productFactory = productFactory;
	}

	public ViewProductDetailsOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(ViewProductDetailsInputData input) {
		outputData = new ViewProductDetailsOutputData();
		
		try {
			// 1. Lấy DTO
			ProductData productData = productRepository.findById(input.productId);
			if(productData == null) {
				throw new IllegalArgumentException("Không tìm thấy sản phẩm.");
			}
			
			// 2. Lấy CategoryData
            CategoryData categoryData = categoryRepository.findById(productData.categoryId);
            if (categoryData == null) {
                throw new IllegalArgumentException("Lỗi: Không tìm thấy loại sản phẩm của sản phẩm này.");
            }
            
            // 3. Chuyển T3 DTOs -> T4 Entity (Dùng Factory)
            Product productEntity = productFactory.load(productData, categoryData);
            
            // 4. Chuyển dữ liệu thành output data
            ProductOutputData safeOutput = mapEntityToOutputData(productEntity, categoryData);
            
        	// 5. Báo cáo thành công
            outputData.success = true;
            outputData.product = safeOutput;
		} catch (IllegalArgumentException e) {
            // 6. Bắt lỗi nghiệp vụ (Không tìm thấy)
            outputData.success = false;
            outputData.message = e.getMessage();
        } catch (Exception e) {
            // 7. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống khi tải sản phẩm.";
        }
		
		productPresenter.present(outputData);
	}

	private ProductOutputData mapEntityToOutputData(Product entity, CategoryData category) {
		ProductOutputData dto = new ProductOutputData();
        
        // 1. Gán thuộc tính chung (Từ Lớp Cha T4)
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.description = entity.getDescription();
        dto.price = entity.getPrice();
        dto.stockQuantity = entity.getStockQuantity();
        dto.imageUrl = entity.getImageUrl();
        dto.categoryId = entity.getCategoryId();
        dto.categoryName = (category != null) ? category.name : "Không rõ";
        
        // 2. "Làm phẳng" (Flatten) thuộc tính riêng (DÙNG ĐA HÌNH)
        // UseCase T3 không cần biết 'entity' là Laptop hay Mouse.
        // Nó chỉ gọi hàm 'abstract' của Lớp Cha T4.
        Map<String, String> specifics = entity.getSpecificAttributes();
        
        // (Gán tất cả các trường có thể có - T3 DTO là "phẳng")
        dto.cpu = specifics.get("cpu"); // (sẽ là null nếu là Mouse)
        dto.ram = (specifics.get("ram") != null) ? Integer.parseInt(specifics.get("ram")) : 0;
        dto.screenSize = specifics.get("screenSize");
        dto.connectionType = specifics.get("connectionType"); // (sẽ là null nếu là Laptop)
        dto.dpi = (specifics.get("dpi") != null) ? Integer.parseInt(specifics.get("dpi")) : 0;
        dto.switchType = specifics.get("switchType");
        dto.layout = specifics.get("layout");
        
        return dto;
	}

}
