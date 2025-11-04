package Product.AddNewProduct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Category.FakeCategoryRepository;
import Product.FakeProductRepository;
import adapters.ManageProduct.AddNewProduct.AddNewProductPresenter;
import adapters.ManageProduct.AddNewProduct.AddNewProductViewModel;
import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageProduct.ProductData;
import application.dtos.ManageProduct.AddNewProduct.AddNewProductInputData;
import application.factories.ManageProduct.ProductFactory;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageProduct.ProductRepository;
import application.usecases.ManageProduct.AddNewProduct.AddNewProductUsecase;

public class TestAddNewProductUseCase {
	private AddNewProductUsecase useCase;
    private ProductRepository productRepo;
    private CategoryRepository categoryRepo;
    private AddNewProductPresenter presenter;
    private AddNewProductViewModel viewModel;
    private ProductFactory productFactory;
    private CategoryData laptopCategory;
    private CategoryData keyboardCategory;
    
    @BeforeEach
    public void setup() {
        productRepo = new FakeProductRepository();
        categoryRepo = new FakeCategoryRepository();
        viewModel = new AddNewProductViewModel();
        presenter = new AddNewProductPresenter(viewModel);
        productFactory = new ProductFactory(); // Factory "Thật"
        
        useCase = new AddNewProductUsecase(
            productRepo, categoryRepo, presenter, productFactory
        );
        
        // Dữ liệu mồi
        laptopCategory = categoryRepo.save(new CategoryData(0, "Laptop", "{}")); // ID: 1
        keyboardCategory = categoryRepo.save(new CategoryData(0, "Keyboard", "{}"));
    }
    
    @Test
    public void testExecute_SuccessCase_AddLaptop() {
        // 1. Arrange (InputData chứa Map thuộc tính riêng)
        Map<String, String> attributes = Map.of(
            "cpu", "Core i7",
            "ram", "16",
            "screenSize", "15.6 inch"
        );
        AddNewProductInputData input = new AddNewProductInputData(
            "Dell XPS", "desc", 1500.0, 10, "img", laptopCategory.id, attributes
        );

        // 2. Act
        useCase.execute(input);

        // 3. Assert (Kiểm tra ViewModel)
        assertEquals(true, useCase.getOutputData().success);
        assertNotNull(useCase.getOutputData().message);
        assertEquals(1, useCase.getOutputData().newProduct.id);
        assertEquals("Dell XPS", useCase.getOutputData().newProduct.name);
        
        // Kiểm tra CSDL giả (Repo đã lưu đúng)
        ProductData savedData = productRepo.findById(1);
        assertEquals("Core i7", savedData.cpu);
        assertEquals(16, savedData.ram);
    }
    
    @Test
    public void testExecute_Fail_Validation_Common() {
        AddNewProductInputData input = new AddNewProductInputData(
            "Dell XPS", "desc", -100.0, 10, "img", laptopCategory.id, Map.of()
        );
        useCase.execute(input);
        assertEquals(false, useCase.getOutputData().success);
        assertEquals("Giá sản phẩm không được âm.", useCase.getOutputData().message);
    }
    
    @Test
    public void testExecute_Fail_Validation_Specific() {
        // 1. Arrange (RAM = 0, vi phạm Laptop.validateSpecific)
        Map<String, String> attributes = Map.of("cpu", "i7", "ram", "0");
        AddNewProductInputData input = new AddNewProductInputData(
            "Dell XPS", "desc", 1500.0, 10, "img", laptopCategory.id, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (UseCase bắt lỗi từ Factory)
        assertEquals(false, useCase.getOutputData().success);
        assertEquals("Thuộc tính Laptop không hợp lệ: RAM phải là số dương.", useCase.getOutputData().message);
    }
    
    @Test
    public void testExecute_Fail_Validation_Specific_Keyboard() {
        // 1. Arrange (Switch trống)
        Map<String, String> attributes = Map.of("switchType", "", "layout", "Full");
        AddNewProductInputData input = new AddNewProductInputData(
            "Feker", "desc", 100.0, 10, "img", keyboardCategory.id, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (UseCase bắt lỗi từ Factory)
        assertEquals(false, useCase.getOutputData().success);
        assertEquals("Thuộc tính Bàn phím không hợp lệ: Loại switch không được để trống.", useCase.getOutputData().message);
    }
}
