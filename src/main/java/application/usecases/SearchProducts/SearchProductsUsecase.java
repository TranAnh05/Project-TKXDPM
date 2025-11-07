package application.usecases.SearchProducts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageProduct.ProductData;
import application.dtos.ManageProduct.ProductOutputData;
import application.dtos.SearchProducts.SearchProductsInputData;
import application.dtos.SearchProducts.SearchProductsOutputData;
import application.factories.ManageProduct.ProductFactory;
import application.ports.in.SearchProducts.SearchProductsInputBoundary;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageProduct.ProductRepository;
import application.ports.out.SearchProducts.SearchProductsOutputBoundary;
import domain.entities.Category;
import domain.entities.Keyboard;
import domain.entities.Laptop;
import domain.entities.Mouse;
import domain.entities.Product;

public class SearchProductsUsecase implements SearchProductsInputBoundary{
	private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private SearchProductsOutputBoundary productPresenter;
    private ProductFactory productFactory;
    private SearchProductsOutputData outputData; // Field cho TDD
    
    public SearchProductsUsecase() {}
    
	public SearchProductsUsecase(ProductRepository productRepository, CategoryRepository categoryRepository,
			SearchProductsOutputBoundary productPresenter, ProductFactory productFactory) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.productPresenter = productPresenter;
		this.productFactory = productFactory;
	}
	
	public SearchProductsOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(SearchProductsInputData input) {
		outputData = new SearchProductsOutputData();
		
		try {
			// 1. Lấy dữ liệu thô (Raw Data) TỪ HÀM TÌM KIẾM
            List<ProductData> productDataList = productRepository.searchByName(input.keyword);
            // 2. Xử lý kịch bản rỗng
            if (productDataList.isEmpty()) {
                outputData.success = true;
                outputData.message = "Không tìm thấy sản phẩm nào khớp với '" + input.keyword + "'.";
                outputData.products = new ArrayList<>();
                productPresenter.present(outputData);
                return;
            }
            
            // 3. Chuẩn bị Map<Integer, CategoryData> (T3 DTO) để tra cứu
            Map<Integer, CategoryData> categoryDataMap = mapCategoriesToData(categoryRepository.findAll());

            // 4. (Bước quan trọng) Chuyển T3 DTOs -> T4 Entities
            List<Product> productEntities = mapDataToEntities(productDataList, categoryDataMap);

            // 5. Chuyển T4 Entities -> T3 Output DTOs (làm phẳng & làm giàu)
            List<ProductOutputData> safeOutputList = mapEntitiesToOutputData(productEntities, categoryDataMap);

            // 6. Báo cáo thành công
            outputData.success = true;
            outputData.products = safeOutputList;
            
		} catch (Exception e) {
			// 7. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống khi tìm kiếm sản phẩm.";
            outputData.products = new ArrayList<>();
		}
		
		productPresenter.present(outputData);
	}

	private List<ProductOutputData> mapEntitiesToOutputData(List<Product> entities,
			Map<Integer, CategoryData> categories) {
		
		List<ProductOutputData> dtoList = new ArrayList<>();
        for (Product entity : entities) {
            dtoList.add(mapEntityToOutputData(entity, categories.get(entity.getCategoryId())));
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
