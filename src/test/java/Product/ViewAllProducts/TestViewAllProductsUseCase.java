package Product.ViewAllProducts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Category.FakeCategoryRepository;
import Product.FakeProductRepository;
import adapters.ManageProduct.AddNewProduct.ProductViewDTO;
import adapters.ManageProduct.ViewAllProducts.ViewAllProductsPresenter;
import adapters.ManageProduct.ViewAllProducts.ViewAllProductsViewModel;
import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageProduct.ProductData;
import application.dtos.ManageProduct.ProductOutputData;
import application.factories.ManageProduct.ProductFactory;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageProduct.ProductRepository;
import application.usecases.ManageProduct.ViewAllProducts.ViewAllProductsUsecase;
import domain.entities.Laptop;
import domain.entities.Mouse;
import domain.entities.Product;

public class TestViewAllProductsUseCase {
	private ViewAllProductsUsecase useCase;
	private ProductRepository productRepo;
	private CategoryRepository categoryRepo;
	private ViewAllProductsPresenter presenter;
    private ViewAllProductsViewModel viewModel;
    private ProductFactory productFactory;
    
    @BeforeEach
    public void setup() {
        productRepo = new FakeProductRepository();
        categoryRepo = new FakeCategoryRepository();
        viewModel = new ViewAllProductsViewModel();
        presenter = new ViewAllProductsPresenter(viewModel);
        productFactory = new ProductFactory(); // Factory "Thật"
        
        useCase = new ViewAllProductsUsecase(
            productRepo, categoryRepo, presenter, productFactory
        );
    }
    
    @Test
    public void testExecute_SuccessCase_WithData() {
        // 1. Arrange (Thêm 1 Laptop và 1 Mouse vào CSDL Giả)
        CategoryData catLaptop = categoryRepo.save(new CategoryData(0, "Laptop", "{}")); // ID 1
        CategoryData catMouse = categoryRepo.save(new CategoryData(0, "Mouse", "{}")); // ID 2
        
        // Dùng T4 Entity để tạo, rồi dùng T3 DTO để lưu (giả lập UC6)
        Laptop laptop = new Laptop("Dell", "desc", 1500, 10, "img", catLaptop.id, "i7", 16, "15in");
        productRepo.save(mapEntityToData(laptop)); // ID 1
        
        Mouse mouse = new Mouse("Logitech", "desc", 100, 50, "img", catMouse.id, "Wireless", 1600);
        productRepo.save(mapEntityToData(mouse)); // ID 2

        // 2. Act (Sửa lại)
        useCase.execute();
        
        System.out.println(useCase.getOutputData().products.size());

        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals(true, useCase.getOutputData().success);
        assertEquals(2, useCase.getOutputData().products.size());
        
        // Kiểm tra Laptop (ID 1)
        ProductOutputData vmLaptop = useCase.getOutputData().products.get(0);
        assertEquals(1, vmLaptop.id);
        assertEquals("Dell", vmLaptop.name);
        assertEquals("Laptop", vmLaptop.categoryName);
        assertEquals("i7", vmLaptop.cpu);
        assertEquals(16, vmLaptop.ram);
        
        // Kiểm tra Mouse (ID 2)
        ProductOutputData vmMouse = useCase.getOutputData().products.get(1);
        assertEquals(2, vmMouse.id);
        assertEquals("Logitech", vmMouse.name);
        assertEquals("Mouse", vmMouse.categoryName);
        assertEquals("Wireless", vmMouse.connectionType);
        assertEquals(1600, vmMouse.dpi);
    }

	// Hàm helper (chỉ dùng cho Test setup)
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
    
//    public static void main(String[] args) {
//    	TestViewAllProductsUseCase usecase = new TestViewAllProductsUseCase();
//    	usecase.setup();
//    	usecase.testExecute_SuccessCase_WithData();
//	}
}
