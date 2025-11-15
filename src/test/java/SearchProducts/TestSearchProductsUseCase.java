package SearchProducts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Category.FakeCategoryRepository;
import Entities.Laptop;
import Entities.Mouse;
import Entities.Product;
import Product.FakeProductRepository;
import application.factories.ManageProduct.ProductFactory;
import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageProduct.ProductData;
import usecase.ManageProduct.SearchProducts.SearchProductsInputData;
import usecase.ManageProduct.SearchProducts.SearchProductsOutputData;
import usecase.ManageProduct.SearchProducts.SearchProductsUsecase;


public class TestSearchProductsUseCase {
	private SearchProductsUsecase useCase;
    private ProductRepository productRepo;
    private CategoryRepository categoryRepo;
    private ProductFactory productFactory;

    @BeforeEach
    public void setup() {
        productRepo = new FakeProductRepository();
        categoryRepo = new FakeCategoryRepository();
        productFactory = new ProductFactory();
        
        useCase = new SearchProductsUsecase(
            productRepo, categoryRepo, null, productFactory
        );
        
        // Dữ liệu mồi
        CategoryData catLaptop = categoryRepo.save(new CategoryData(0, "Laptop", "{}"));
        CategoryData catMouse = categoryRepo.save(new CategoryData(0, "Mouse", "{}"));
        
        Product p1 = new Laptop("Dell XPS 15", "desc", 1500, 10, "img", catLaptop.id, "i7", 16, "15in");
        Product p2 = new Mouse("Logitech MX", "desc", 100, 50, "img", catMouse.id, "Wireless", 1600);
        Product p3 = new Laptop("Dell Vostro", "desc", 800, 20, "img", catLaptop.id, "i5", 8, "14in");
        
        productRepo.save(mapEntityToData(p1)); // ID 1
        productRepo.save(mapEntityToData(p2)); // ID 2
        productRepo.save(mapEntityToData(p3)); // ID 3
    }
    
    @Test
    public void testExecute_SuccessCase_FoundMultiple() {
        // 1. Arrange: Tìm "Dell"
        SearchProductsInputData input = new SearchProductsInputData("Dell");
        
        // 2. Act
        useCase.execute(input);

        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        SearchProductsOutputData output =  useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals(2, output.products.size()); // Tìm thấy 2 sản phẩm "Dell"
        assertEquals("Dell XPS 15", output.products.get(0).name);
        assertEquals("Dell Vostro", output.products.get(1).name);
        assertEquals("Laptop", output.products.get(0).categoryName); // Đã làm giàu
    }
    
    @Test
    public void testExecute_SuccessCase_NoData() {
        // 1. Arrange: Tìm "Asus" (không có)
        SearchProductsInputData input = new SearchProductsInputData("");
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        SearchProductsOutputData output = useCase.getOutputData();
        assertEquals("Không tìm thấy sản phẩm nào khớp với ''.", output.message);
        assertTrue(output.success);
        assertEquals(0, output.products.size());
    }

	private ProductData mapEntityToData(Product product) {
		ProductData data = new ProductData();
		
        data.name = product.getName();
        data.description = product.getDescription();
        data.price = product.getPrice();
        data.stockQuantity = product.getStockQuantity();
        data.imageUrl = product.getImageUrl();
        data.categoryId = product.getCategoryId();
        if (product instanceof Laptop) {
            data.cpu = ((Laptop) product).getCpu();
            data.ram = ((Laptop) product).getRam();
        } else if (product instanceof Mouse) {
            data.connectionType = ((Mouse) product).getConnectionType();
            data.dpi = ((Mouse) product).getDpi();
        }
        
        return data;
	}
}
