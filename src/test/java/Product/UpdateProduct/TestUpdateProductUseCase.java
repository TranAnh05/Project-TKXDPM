package Product.UpdateProduct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Category.FakeCategoryRepository;
import Product.FakeProductRepository;
import adapters.ManageProduct.UpdateProduct.UpdateProductPresenter;
import adapters.ManageProduct.UpdateProduct.UpdateProductViewModel;
import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageProduct.ProductData;
import application.dtos.ManageProduct.UpdateProduct.UpdateProductInputData;
import application.dtos.ManageProduct.UpdateProduct.UpdateProductOutputData;
import application.factories.ManageProduct.ProductFactory;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageProduct.ProductRepository;
import application.usecases.ManageProduct.UpdateProduct.UpdateProductUsecase;

public class TestUpdateProductUseCase {
	private UpdateProductUsecase useCase;
    private ProductRepository productRepo;
    private CategoryRepository categoryRepo;
    private UpdateProductViewModel viewModel;
    private UpdateProductPresenter presenter;
    private ProductFactory productFactory;
    private CategoryData laptopCategory;
    private ProductData existingLaptopData;
    
    @BeforeEach
    public void setup() {
        productRepo = new FakeProductRepository();
        categoryRepo = new FakeCategoryRepository();
        viewModel = new UpdateProductViewModel();
        presenter = new UpdateProductPresenter(viewModel);
        productFactory = new ProductFactory();
        
        useCase = new UpdateProductUsecase(
            productRepo, categoryRepo, presenter, productFactory
        );
        
        // Dữ liệu mồi
        laptopCategory = categoryRepo.save(new CategoryData(0, "Laptop", "{}"));
        ProductData pData = new ProductData();
        pData.name = "Dell XPS 13"; pData.price = 1300; pData.ram = 8;
        pData.categoryId = laptopCategory.id;
        existingLaptopData = productRepo.save(pData);
    }
    
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange: Sửa Laptop (ID 1)
        Map<String, String> attributes = Map.of("cpu", "i9", "ram", "32", "screenSize", "13in");
        UpdateProductInputData input = new UpdateProductInputData(
            existingLaptopData.id, 
            "Dell XPS 13 (Updated)", "new desc", 1600.0, 5, "new img", 
            laptopCategory.id, attributes
        );

        // 2. Act
        useCase.execute(input);
        UpdateProductOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals(1, output.updatedProduct.id);
        assertEquals("Dell XPS 13 (Updated)", output.updatedProduct.name);
        
        // Kiểm tra CSDL giả
        ProductData updatedDataInDb = productRepo.findById(1);
        assertEquals(1600.0, updatedDataInDb.price);
        assertEquals(32, updatedDataInDb.ram);
    }
    
    @Test
    public void testExecute_Fail_Validation_Specific() {
        // 1. Arrange (Sửa RAM = 0)
        Map<String, String> attributes = Map.of("cpu", "i9", "ram", "0");
        UpdateProductInputData input = new UpdateProductInputData(
            existingLaptopData.id, "Dell XPS 13", "desc", 1500.0, 10, "img",
            laptopCategory.id, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Thuộc tính Laptop không hợp lệ: RAM phải là số dương.", output.message);
    }
}
