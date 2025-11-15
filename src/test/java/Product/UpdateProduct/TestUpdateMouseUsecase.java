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
import usecase.ManageProduct.UpdateProduct.UpdateLaptopUsecase;
import usecase.ManageProduct.UpdateProduct.UpdateMouseUsecase;
import usecase.ManageProduct.UpdateProduct.UpdateProductInputData;
import usecase.ManageProduct.UpdateProduct.UpdateProductOutputBoundary;
import usecase.ManageProduct.UpdateProduct.UpdateProductOutputData;

@ExtendWith(MockitoExtension.class)
public class TestUpdateMouseUsecase {
	// 1. "Giả lập" (Mock) các Dependencies (Port T1 và Port T2)
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private UpdateProductOutputBoundary productPresenter;
    
    // 2. "Tiêm" (Inject) các Mock (ở trên) vào Interactor
    @InjectMocks
    private UpdateMouseUsecase useCase;
    
    // Trường hợp thành công
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange (Sắp xếp)
    	 Map<String, String> attributes = Map.of("connectionType", "có dây", "dpi", "4000");
        UpdateProductInputData input = new UpdateProductInputData(
            2, "Mouse Logitech", "desc", 1500, 10, "img", 2, attributes
        );
        
        // (Dạy Repo trả về DTO T3 "thô")
        CategoryData mockCategoryData = new CategoryData(2, "Mouse");
        ProductData mockProductData = new ProductData(
            2, "Mouse Acer", "desc", 100, 10, "img", 2, // Chung
            null, 0, null, // Laptop
            "test", 2000, null, null // Mouse/Keyboard
        );
        
        
        when(productRepository.findById(2)).thenReturn(mockProductData);
        when(categoryRepository.findById(2)).thenReturn(mockCategoryData);
        when(productRepository.findByName("Mouse Logitech")).thenReturn(null);

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
        assertEquals(2, output.updatedProduct.id);
        assertEquals("Mouse Logitech", output.updatedProduct.name);
        assertEquals("Mouse", output.updatedProduct.categoryName);
    }
    
    // TEST VALIDATION CHUNG -> LAPTOP DA TEST
    
    // TEST VALIDATION RIENG
    @Test
    public void testExecute_Fail_Validation_Specific() {
    	// 1. Arrange (connectionType rỗng, vi phạm Tầng 4)
    	 Map<String, String> attributes = Map.of("connectionType", "", "dpi", "4000");
    	UpdateProductInputData input = new UpdateProductInputData(
           2, "Dell XPS", "desc", 1500, 10, "img", 2, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Test OutputData T3)
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Thuộc tính Mouse không hợp lệ: Loại connectionType không được để trống.", output.message);
        
        // Khẳng định: CSDL KHÔNG bao giờ được gọi
        verify(productRepository, never()).update(any(ProductData.class));
    }
    
    @Test
    public void testExecute_Fail_Validation_Specific02() {
    	 // 1. Arrange (connectionType rỗng, vi phạm Tầng 4)
   	 Map<String, String> attributes = Map.of("connectionType", "test", "dpi", "-4000");
    	UpdateProductInputData input = new UpdateProductInputData(
           2, "Dell XPS", "desc", 1500, 10, "img", 2, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Test OutputData T3)
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Thuộc tính Mouse không hợp lệ: DPI phải là số dương.", output.message);
        
        // Khẳng định: CSDL KHÔNG bao giờ được gọi
        verify(productRepository, never()).update(any(ProductData.class));
    }
    
    // TEST CSDL SAP - DA TEST O LAPTOP
}
