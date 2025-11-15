package Product.ViewAllProducts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Category.FakeCategoryRepository;
import Entities.Laptop;
import Entities.Mouse;
import Entities.Product;
import Product.FakeProductRepository;
import adapters.ManageProduct.AddNewProduct.ProductViewDTO;
import adapters.ManageProduct.ViewAllProducts.ViewAllProductsPresenter;
import adapters.ManageProduct.ViewAllProducts.ViewAllProductsViewModel;
import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesOutputData;
import usecase.ManageProduct.ProductData;
import usecase.ManageProduct.ProductOutputData;
import usecase.ManageProduct.ViewAllProducts.ViewAllProductsOutputBoundary;
import usecase.ManageProduct.ViewAllProducts.ViewAllProductsOutputData;
import usecase.ManageProduct.ViewAllProducts.ViewAllProductsUsecase;

@ExtendWith(MockitoExtension.class)
public class TestViewAllProductsUseCase {
	@Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ViewAllProductsOutputBoundary productPresenter;
    
    @InjectMocks
    private ViewAllProductsUsecase useCase;
    
    @Test
    public void testExecute_SuccessCase_WithData() {
        // 1. Arrange (Dạy Mock)
        List<CategoryData> mockCategoryData = List.of(new CategoryData(1, "Laptop"));
        // (DTO thô từ CSDL)
        List<ProductData> mockProductData = List.of(
            new ProductData(1, "Dell", "desc", 100, 10, "img", 1, "i7", 16, "15in", null, 0, null, null)
        );
        
        when(productRepository.findAll()).thenReturn(mockProductData);
        when(categoryRepository.findAll()).thenReturn(mockCategoryData);

        // 2. Act
        useCase.execute();

        // 3. Assert (Kiểm tra OutputData T3 - Tóm tắt)
        ViewAllProductsOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals(1, output.products.size());
        
        // (Quan trọng) Kiểm tra DTO Tóm tắt (T3)
        assertEquals("Dell", output.products.get(0).name);
        assertEquals(100.0, output.products.get(0).price);
        assertEquals("Laptop", output.products.get(0).categoryName);
    }
    
    @Test
    public void testExecute_SuccessCase_WithNoData() {
        // 1. Arrange (Dạy Mock)
        List<CategoryData> mockCategoryData = List.of();
        // (DTO thô từ CSDL)
        List<ProductData> mockProductData = List.of();
        
        when(productRepository.findAll()).thenReturn(mockProductData);
        when(categoryRepository.findAll()).thenReturn(mockCategoryData);

        // 2. Act
        useCase.execute();

        // 3. Assert (Kiểm tra OutputData T3 - Tóm tắt)
        ViewAllProductsOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals(0, output.products.size());
        assertEquals("Chưa có sản phẩm nào.", output.message);
    }
    
    // trường hợp thất bại do lỗi kết nối với csdl
    @Test
    public void testExecute_Fail_DatabaseError() {
        // 1. Arrange (Dạy Mock ném lỗi hệ thống)
        when(categoryRepository.findAll()).thenThrow(new RuntimeException("CSDL sập"));

        // 2. Act
        useCase.execute();
        
        // 3. Assert
        ViewAllProductsOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Đã xảy ra lỗi hệ thống khi tải sản phẩm.", output.message);
    }
}
