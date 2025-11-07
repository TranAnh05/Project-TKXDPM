package application.usecases.ManageProduct.ViewAllProducts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageProduct.ProductData;
import application.dtos.ManageProduct.ProductOutputData;
import application.dtos.ManageProduct.ViewAllProducts.ViewAllProductsOutputData;
import application.factories.ManageProduct.ProductFactory;
import application.ports.in.ManageProduct.ViewAllProducts.ViewAllProductsInputBoundary;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageProduct.ProductRepository;
import application.ports.out.ManageProduct.ViewAllProducts.ViewAllProductsOutputBoundary;
import domain.entities.Keyboard;
import domain.entities.Laptop;
import domain.entities.Mouse;
import domain.entities.Product;

public class ViewAllProductsUsecase implements ViewAllProductsInputBoundary{
	private ProductRepository productRepository;
	private CategoryRepository categoryRepository;
	private ViewAllProductsOutputBoundary outBoundary;
	private ProductFactory productFactory;
	private ViewAllProductsOutputData outputData;
	
	public ViewAllProductsUsecase() {
		
	}
	
	public ViewAllProductsUsecase(ProductRepository productRepository, CategoryRepository categoryRepository,
			ViewAllProductsOutputBoundary outBoundary, ProductFactory productFactory) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.outBoundary = outBoundary;
		this.productFactory = productFactory;
	}
	
	public ViewAllProductsOutputData getOutputData() {
		return outputData;
	}
	
	@Override
	public void execute() {
		outputData = new ViewAllProductsOutputData();
		
		try {
			// 1. Lấy dữ liệu thô (Raw Data)
	        List<ProductData> productDataList = productRepository.findAll();
	        List<CategoryData> categoryDataList = categoryRepository.findAll();
			
	        // 2. Xử lý kịch bản rỗng
	        if (productDataList.isEmpty()) {
	            outputData.success = true;
	            outputData.message = "Chưa có sản phẩm nào.";
	            outputData.products = new ArrayList<>();
	            outBoundary.present(outputData);
	            return;
	        }
	        
	        Map<Integer, CategoryData> categoryDataMap = mapCategoriesToData(categoryDataList);
	        
	        List<Product> productEntities = mapDataToEntities(productDataList, categoryDataMap);
	        
	        List<ProductOutputData> safeOutputList = mapEntitiesToOutputData(productEntities, categoryDataMap);
	        
	        outputData.success = true;
            outputData.products = safeOutputList;
		} catch (Exception e) {
			// 7. Bắt lỗi hệ thống
			 e.printStackTrace();
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống khi tải sản phẩm.";
            outputData.products = new ArrayList<>();
		}
		
		outBoundary.present(outputData);
	}

	private List<ProductOutputData> mapEntitiesToOutputData(List<Product> entities, Map<Integer, CategoryData> categoryDataMap) {
		List<ProductOutputData> dtoList = new ArrayList<>();
        for (Product entity : entities) {
            dtoList.add(mapEntityToOutputData(entity, categoryDataMap.get(entity.getCategoryId())));
        }
        return dtoList;
	}

	private ProductOutputData mapEntityToOutputData(Product entity, CategoryData category) {
		ProductOutputData dto = new ProductOutputData();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.description = entity.getDescription();
        dto.price = entity.getPrice();
        dto.stockQuantity = entity.getStockQuantity();
        dto.imageUrl = entity.getImageUrl();
        dto.categoryId = entity.getCategoryId();
        dto.categoryName = (category != null) ? category.name : "Không rõ";
        
        // "Làm phẳng" (Flatten) thuộc tính riêng
        if (entity instanceof Laptop) {
            Laptop laptop = (Laptop) entity;
            dto.cpu = laptop.getCpu();
            dto.ram = laptop.getRam();
            dto.screenSize = laptop.getScreenSize();
        } else if (entity instanceof Mouse) {
            Mouse mouse = (Mouse) entity;
            dto.connectionType = mouse.getConnectionType();
            dto.dpi = mouse.getDpi();
        } else if (entity instanceof Keyboard) {
            Keyboard keyboard = (Keyboard) entity;
            dto.switchType = keyboard.getSwitchType();
            dto.layout = keyboard.getLayout();
        }
        return dto;
	}

	private List<Product> mapDataToEntities(List<ProductData> dataList,
			Map<Integer, CategoryData> categoryMap) {
		
		List<Product> entities = new ArrayList<>();
        for (ProductData data : dataList) {
            CategoryData category = categoryMap.get(data.categoryId);
            if (category == null) {
                // Logic phòng thủ: Bỏ qua sản phẩm mồ côi
                System.err.println("Bỏ qua sản phẩm mồ côi: ID " + data.id);
                continue; 
            }
            // Gọi Factory (T3) để "load" (tái tạo) Entity (T4)
            entities.add(productFactory.load(data, category));
        }
        return entities;
	}

	private Map<Integer, CategoryData> mapCategoriesToData(List<CategoryData> dataList) {
		Map<Integer, CategoryData> map = new HashMap<>();
        for (CategoryData data : dataList) { map.put(data.id, data); }
        return map;
	}
	
}
