package application.usecases.ManageProduct.AddNewProduct;

import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageProduct.ProductData;
import application.dtos.ManageProduct.ProductOutputData;
import application.dtos.ManageProduct.AddNewProduct.AddNewProductInputData;
import application.dtos.ManageProduct.AddNewProduct.AddNewProductOutputData;
import application.factories.ManageProduct.ProductFactory;
import application.ports.in.ManageProduct.AddNewProduct.AddNewProductInputBoundary;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageProduct.ProductRepository;
import application.ports.out.ManageProduct.AddNewProduct.AddNewProductOutputBoundary;
import domain.entities.Keyboard;
import domain.entities.Laptop;
import domain.entities.Mouse;
import domain.entities.Product;

public class AddNewProductUsecase implements AddNewProductInputBoundary{
	private ProductRepository productRepository;
	private CategoryRepository categoryRepository;
	private AddNewProductOutputBoundary outBoundary;
	private ProductFactory productFactory;
	
	private AddNewProductOutputData outputData;
	
	private AddNewProductUsecase() {
		
	}
	
	public AddNewProductUsecase(ProductRepository productRepository, CategoryRepository categoryRepository,
			AddNewProductOutputBoundary outBoundary, ProductFactory productFactory) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.outBoundary = outBoundary;
		this.productFactory = productFactory;
	}
	
	public AddNewProductOutputData getOutputData() {
        return this.outputData;
    }

	@Override
	public void execute(AddNewProductInputData input) {
		outputData = new AddNewProductOutputData();
		
		try {
			// 1. Kiểm tra nghiệp vụ (Tên trùng)
            if (productRepository.findByName(input.name) != null) {
                throw new IllegalArgumentException("Tên sản phẩm này đã tồn tại.");
            }
            
            // 2. Kiểm tra Category
            CategoryData categoryData = categoryRepository.findById(input.categoryId);
            if (categoryData == null) {
                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ.");
            }
            
            // 3. GỌI FACTORY (T3) ĐỂ TẠO VÀ VALIDATE (T4)
            // (Factory sẽ ném lỗi nếu validation T4 thất bại)
            Product productEntity = productFactory.create(
                input.name, input.description, input.price, input.stockQuantity, 
                input.imageUrl, categoryData, input.attributes
            );
            
            // 4. Lưu vào CSDL (Tầng 3 gửi Entity, Tầng 1 trả về DTO)
            ProductData dataToSave = mapEntityToData(productEntity);
            
            // 5. Lưu vào CSDL (Repository T1 chỉ nhận T3 DTO)
            ProductData savedData = productRepository.save(dataToSave);
            
            outputData.success = true;
            outputData.message = "Thêm sản phẩm thành công!";
            outputData.newProduct = mapDataToOutput(savedData, categoryData.name);
            
		} catch (IllegalArgumentException e) {
            // 6. BẮT LỖI VALIDATION (T4/Factory) HOẶC LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
            
        }catch (Exception e) {
        	// 7. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống.";
		}
		
		outBoundary.present(outputData);
		
	}

	private ProductData mapEntityToData(Product product) {
		ProductData data = new ProductData();
        // 1. Gán thuộc tính chung
        data.id = 0; // ID 0 để tạo mới
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

}
