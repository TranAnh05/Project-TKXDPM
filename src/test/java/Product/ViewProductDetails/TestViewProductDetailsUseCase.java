package Product.ViewProductDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import application.factories.ManageProduct.ProductFactory;
import application.ports.out.ManageProduct.ProductRepository;
import application.usecases.ManageProduct.ViewProductDetails.ViewProductDetailsUsecase;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageProduct.ProductData;
import usecase.ManageProduct.ViewAllProducts.ViewAllProductsOutputData;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsInputData;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsOutputBoundary;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsOutputData;

@ExtendWith(MockitoExtension.class)
public class TestViewProductDetailsUseCase {
	// Mock (Giả) các Repository và Presenter
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ViewProductDetailsOutputBoundary productPresenter;
    
    // Spy (Thật) Factory (vì chúng ta cần logic 'load' của nó)
    @Spy
    private ProductFactory productFactory;
    
    // Tiêm (Inject) 4 đối tượng trên vào Interactor
    @InjectMocks
    private ViewProductDetailsUsecase useCase;
    
    @Test
    public void testExecute_SuccessCase_ViewLaptop() {
        // 1. Arrange (Dạy Mock)
        ViewProductDetailsInputData input = new ViewProductDetailsInputData(1); // Tìm ID 1
        
        // (Dạy Repo trả về DTO T3 "thô")
        CategoryData mockCategoryData = new CategoryData(1, "Laptop");
        ProductData mockProductData = new ProductData(
            1, "Dell", "desc", 100, 10, "img", 1, // Chung
            "i7", 16, "15in", // Laptop
            null, 0, null, null // Mouse/Keyboard
        );
        
        when(productRepository.findById(1)).thenReturn(mockProductData);
        when(categoryRepository.findById(1)).thenReturn(mockCategoryData);

        // 2. Act
        useCase.execute(input);

        // 3. Assert (Kiểm tra OutputData T3)
        ViewProductDetailsOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        
        // Kiểm tra xem logic Đa hình (T4) và map (T3) có đúng không
        assertEquals(1, output.product.id);
        assertEquals("Dell", output.product.name);
        assertEquals("Laptop", output.product.categoryName);
        assertEquals("i7", output.product.cpu); // <-- Phải có
        assertEquals(16, output.product.ram); // <-- Phải có
        assertEquals(0,output.product.dpi); // <-- Phải 0
    }
    
    @Test
    public void testExecute_Fail_NotFound() {
        // 1. Arrange (Dạy Mock)
        ViewProductDetailsInputData input = new ViewProductDetailsInputData(99);
        
        // Dạy: "Khi findById(99) được gọi, HÃY trả về null"
        when(productRepository.findById(99)).thenReturn(null);

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Kiểm tra OutputData T3)
        ViewProductDetailsOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Không tìm thấy sản phẩm.", output.message);
    }
    
    // trường hợp thất bại do lỗi kết nối với csdl
    @Test
    public void testExecute_DatabaseError() {
        // 1. Arrange
        ViewProductDetailsInputData input = new ViewProductDetailsInputData(1);

        // Giả lập repository bị lỗi CSDL
        when(productRepository.findById(1)).thenThrow(new RuntimeException("Database connection error"));

        // 2. Act
        useCase.execute(input);

        // 3. Assert
        ViewProductDetailsOutputData output = useCase.getOutputData();

        // Kiểm tra có bắt lỗi và set đúng trạng thái không
        assertFalse(output.success);
        assertEquals("Đã xảy ra lỗi hệ thống khi tải sản phẩm.", output.message);
    }
}
