package Product.UpdateProduct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;
import usecase.ManageProduct.ProductData;
import usecase.ManageProduct.AddNewProduct.AddNewProductInputData;
import usecase.ManageProduct.AddNewProduct.AddNewProductOutputData;
import usecase.ManageProduct.UpdateProduct.UpdateKeyboardUsecase;
import usecase.ManageProduct.UpdateProduct.UpdateMouseUsecase;
import usecase.ManageProduct.UpdateProduct.UpdateProductInputData;
import usecase.ManageProduct.UpdateProduct.UpdateProductOutputBoundary;
import usecase.ManageProduct.UpdateProduct.UpdateProductOutputData;

@ExtendWith(MockitoExtension.class)
public class TestUpdateKeyboardUsecase {
	// 1. "Giả lập" (Mock) các Dependencies (Port T1 và Port T2)
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private UpdateProductOutputBoundary productPresenter;
    
    // 2. "Tiêm" (Inject) các Mock (ở trên) vào Interactor
    @InjectMocks
    private UpdateKeyboardUsecase useCase;
    
 // Trường hợp thành công
    @Test
    public void testExecute_SuccessCase() {
    	// 1. Arrange (Sắp xếp)
        Map<String, String> attributes = Map.of("switchType", "test", "layout", "test");
        UpdateProductInputData input = new UpdateProductInputData(
            3, "Keyboard Test", "desc", 1500, 10, "img", 3, attributes
        );
        
        // (Dạy Repo trả về DTO T3 "thô")
        CategoryData mockCategoryData = new CategoryData(3, "Keyboard");
        ProductData mockProductData = new ProductData(
            3, "Keyboard", "desc", 100, 10, "img", 3, // Chung
            null, 0, null, // Laptop
            null, 0, "test", "test" // Mouse/Keyboard
        );
        
        
        when(productRepository.findById(3)).thenReturn(mockProductData);
        when(categoryRepository.findById(3)).thenReturn(mockCategoryData);
        when(productRepository.findByName("Keyboard Test")).thenReturn(null);

        when(productRepository.update(any(ProductData.class)))
        .thenAnswer(invocation -> {
            // Trả lại chính đối tượng (argument 0) đã được gửi vào
            return (ProductData) invocation.getArgument(0); 
        });
        
        // 2. Act
        useCase.execute(input);

        // 3. Assert (Kiểm tra OutputData T3)
        UpdateProductOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        
        // Kiểm tra xem logic Đa hình (T4) và map (T3) có đúng không
        assertEquals(3, output.updatedProduct.id);
        assertEquals("Keyboard Test", output.updatedProduct.name);
        assertEquals("Keyboard", output.updatedProduct.categoryName);
    }
    
    // TEST VALIDATION CHUNG -> LAPTOP DA TEST
    
    // TEST VALIDATE RIÊNG
    // trường hợp test thất bại
    @Test
    public void testExecute_Fail_Validation_Specific() {
        // 1. Arrange (switchType rỗng, vi phạm Tầng 4)
    	Map<String, String> attributes = Map.of("switchType", "", "layout", "test");
    	UpdateProductInputData input = new UpdateProductInputData(
           3, "Dell XPS", "desc", 1500, 10, "img", 3, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Test OutputData T3)
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Thuộc tính Keyboard không hợp lệ: Loại switch không được để trống.", output.message);
        
        // Khẳng định: CSDL KHÔNG bao giờ được gọi
        verify(productRepository, never()).update(any(ProductData.class));
    }
    
    @Test
    public void testExecute_Fail_Validation_Specific02() {
        // 1. Arrange (switchType rỗng, vi phạm Tầng 4)
    	Map<String, String> attributes = Map.of("switchType", "test", "layout", "");
    	UpdateProductInputData input = new UpdateProductInputData(
           3, "Dell XPS", "desc", 1500, 10, "img", 3, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Test OutputData T3)
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Thuộc tính Keyboard không hợp lệ: Loại layout không được để trống.", output.message);
        
        // Khẳng định: CSDL KHÔNG bao giờ được gọi
        verify(productRepository, never()).update(any(ProductData.class));
    }
    
}
