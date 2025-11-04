package application.usecases.ManageProduct.UpdateProduct;

import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageProduct.ProductData;
import application.dtos.ManageProduct.ProductOutputData;
import application.dtos.ManageProduct.UpdateProduct.UpdateProductInputData;
import application.dtos.ManageProduct.UpdateProduct.UpdateProductOutputData;
import application.factories.ManageProduct.ProductFactory;
import application.ports.in.ManageProduct.UpdateProduct.UpdateProductInputBoundary;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageProduct.ProductRepository;
import application.ports.out.ManageProduct.UpdateProduct.UpdateProductOutputBoundary;
import domain.entities.Keyboard;
import domain.entities.Laptop;
import domain.entities.Mouse;
import domain.entities.Product;

public class UpdateProductUsecase implements UpdateProductInputBoundary{
	private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private UpdateProductOutputBoundary productPresenter;
    private ProductFactory productFactory;
    
    private UpdateProductOutputData outputData; 
    
    public UpdateProductUsecase() {
    	
    }
	
	public UpdateProductUsecase(ProductRepository productRepository, CategoryRepository categoryRepository,
			UpdateProductOutputBoundary productPresenter, ProductFactory productFactory) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.productPresenter = productPresenter;
		this.productFactory = productFactory;
	}
	
	public UpdateProductOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(UpdateProductInputData input) {
		outputData = new UpdateProductOutputData();
		
		try {
			// 1. Kiểm tra nghiệp vụ (Tên trùng)
			ProductData existingByName = productRepository.findByName(input.name);
            if (existingByName != null && existingByName.id != input.id) {
                throw new IllegalArgumentException("Tên sản phẩm này đã tồn tại.");
            }
            
            // 2. Lấy Dữ liệu cũ (Raw Data)
            ProductData productData = productRepository.findById(input.id);
            if (productData == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm để cập nhật.");
            }
            
            CategoryData categoryData = categoryRepository.findById(productData.categoryId);
            if (categoryData == null) {
                throw new IllegalArgumentException("Lỗi: Không tìm thấy loại sản phẩm của sản phẩm này.");
            }
            
            // 3. TÁI TẠO Entity (T4)
            Product productEntity = productFactory.load(productData, categoryData);
            
            // 4. GỌI TẦNG 4 ĐỂ CẬP NHẬT VÀ VALIDATE
            // (Hàm này sẽ 'throw' nếu dữ liệu mới (Input) không hợp lệ)
            productEntity.updateCommon(input.name, input.description, input.price, input.stockQuantity, input.imageUrl);
            productEntity.updateSpecific(input.attributes); // Cập nhật thuộc tính riêng
            
            // 5. Chuyển T4 (Entity) -> T3 (DTO)
            ProductData dataToUpdate = mapEntityToData(productEntity);
	            
	         // 6. Lưu vào CSDL
            ProductData savedData = productRepository.update(dataToUpdate);
            
         	// 7. Báo cáo thành công (Làm giàu DTO)
            outputData.success = true;
         	outputData.message = "Cập nhật sản phẩm thành công!";
         	outputData.updatedProduct = mapDataToOutput(savedData, categoryData.name);
            
		} catch (IllegalArgumentException e) {
            // 8. BẮT LỖI VALIDATION (T4) HOẶC LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
        } catch (Exception e) {
        	// 9. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống.";
		}
		
		productPresenter.present(outputData);
	}

	private ProductOutputData mapDataToOutput(ProductData data, String name) {
		ProductOutputData dto = new ProductOutputData();
        dto.id = data.id;
        dto.name = data.name;
        dto.description = data.description;
        dto.price = data.price;
        dto.stockQuantity = data.stockQuantity;
        dto.imageUrl = data.imageUrl;
        dto.categoryId = data.categoryId;
        dto.categoryName = name; // <-- Làm giàu
        
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

	private ProductData mapEntityToData(Product product) {
		ProductData data = new ProductData();
        // 1. Gán thuộc tính chung
        data.id = product.getId();
        data.name = product.getName();
        data.description = product.getDescription();
        data.price = product.getPrice();
        data.stockQuantity = product.getStockQuantity();
        data.imageUrl = product.getImageUrl();
        data.categoryId = product.getCategoryId();
        
        // 2. Gán thuộc tính riêng (dùng instanceof)
        if (product instanceof Laptop) {
            Laptop laptop = (Laptop) product;
            data.cpu = laptop.getCpu();
            data.ram = laptop.getRam();
            data.screenSize = laptop.getScreenSize();
        } else if (product instanceof Mouse) {
            Mouse mouse = (Mouse) product;
            data.connectionType = mouse.getConnectionType();
            data.dpi = mouse.getDpi();
        } else if (product instanceof Keyboard) {
            Keyboard keyboard = (Keyboard) product;
            data.switchType = keyboard.getSwitchType();
            data.layout = keyboard.getLayout();
        }

        return data;
	}

}
