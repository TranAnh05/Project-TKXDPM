package Product.AddNewProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import usecase.ManageProduct.AddNewProduct.AddNewKeyBoardUsecase;
import usecase.ManageProduct.AddNewProduct.AddNewMouseUsecase;
import usecase.ManageProduct.AddNewProduct.AddNewProductInputData;
import usecase.ManageProduct.AddNewProduct.AddNewProductOutputBoundary;
import usecase.ManageProduct.AddNewProduct.AddNewProductOutputData;

@ExtendWith(MockitoExtension.class)
public class TestAddNewKeyboardUsecase {
	// 1. "Giả lập" (Mock) các Dependencies (Port T1 và Port T2)
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private AddNewProductOutputBoundary productPresenter;
    
    // 2. "Tiêm" (Inject) các Mock (ở trên) vào Interactor
    @InjectMocks
    private AddNewKeyBoardUsecase useCase; // (Tên class T3 CON của bạn)
    
    // Trường hợp thành công
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange (Sắp xếp)
        Map<String, String> attributes = Map.of("switchType", "test", "layout", "test");
        AddNewProductInputData input = new AddNewProductInputData(
            "Dell XPS", "desc", 1500, 10, "img", 1, attributes
        );
        
        // "Dạy" (Stub) cho Mock Repository (T1):
        when(productRepository.findByName("Dell XPS")).thenReturn(null);
        
        // "Dạy" (Stub) cho Mock Repository (T1):
        CategoryData category = new CategoryData(1, "Laptop");
        when(categoryRepository.findById(1)).thenReturn(category);
        
        // "Dạy" (Stub) cho Mock Repository (T1):
        ProductData savedData = new ProductData(); // DTO "thô"
        savedData.id = 1;
        savedData.name = "Dell XPS";
        when(productRepository.save(any(ProductData.class))).thenReturn(savedData);

        // 2. Act (Hành động)
        useCase.execute(input);

        // 3. Assert (Khẳng định)
        // (Test Tầng 3 - Yêu cầu TDD 208: Chỉ test OutputData)
        AddNewProductOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals(1, output.newProduct.id);
        
        // (Tùy chọn) Kiểm tra xem Presenter (T2) có được gọi đúng 1 lần không
        verify(productPresenter, times(1)).present(output);
    }
    
    // TEST VALIDATE CHUNG
    @Test
    public void testExecute_Fail_Validation_Common() {
        // 1. Arrange (Giá âm)
    	Map<String, String> attributes = Map.of("switchType", "test", "layout", "test");
        AddNewProductInputData input = new AddNewProductInputData(
            "Dell XPS", "desc", -100, 10, "img", 1, attributes
        );

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        AddNewProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Giá sản phẩm không được âm.", output.message);
        
        verify(productRepository, never()).save(any(ProductData.class));
    }
    
    @Test
    public void testExecute_Fail_Validation_Common02() {
        // 1. Arrange (Tồn kho âm)
    	Map<String, String> attributes = Map.of("switchType", "test", "layout", "test");
        AddNewProductInputData input = new AddNewProductInputData(
            "Dell XPS", "desc", 100, -10, "img", 1, attributes
        );

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        AddNewProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Số lượng tồn kho không được âm.", output.message);
        
        verify(productRepository, never()).save(any(ProductData.class));
    }
    
    
    @Test
    public void testExecute_Fail_Validation_Common03() {
        // 1. Arrange (tên rỗng)
    	Map<String, String> attributes = Map.of("switchType", "test", "layout", "test");
        AddNewProductInputData input = new AddNewProductInputData(
            "", "desc", 100, 10, "img", 1, attributes
        );

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        AddNewProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Tên sản phẩm không được để trống.", output.message);
        
        verify(productRepository, never()).save(any(ProductData.class));
    }
    
    @Test
    public void testExecute_Fail_Validation_Common04() {
        // 1. Arrange (mô tả rỗng)
    	Map<String, String> attributes = Map.of("switchType", "test", "layout", "test");
        AddNewProductInputData input = new AddNewProductInputData(
            "test name", "", 100, 10, "img", 1, attributes
        );

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        AddNewProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Mô tả sản phẩm không được để trống.", output.message);
        
        verify(productRepository, never()).save(any(ProductData.class));
    }
    
    // TEST VALIDATE RIÊNG
    // trường hợp test thất bại
    @Test
    public void testExecute_Fail_Validation_Specific() {
        // 1. Arrange (switchType rỗng, vi phạm Tầng 4)
    	Map<String, String> attributes = Map.of("switchType", "", "layout", "test");
        AddNewProductInputData input = new AddNewProductInputData(
            "Dell XPS", "desc", 1500, 10, "img", 1, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Test OutputData T3)
        AddNewProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Thuộc tính Keyboard không hợp lệ: Loại switch không được để trống.", output.message);
        
        // Khẳng định: CSDL KHÔNG bao giờ được gọi
        verify(productRepository, never()).save(any(ProductData.class));
    }
    
    @Test
    public void testExecute_Fail_Validation_Specific02() {
        // 1. Arrange (layout rỗng, vi phạm Tầng 4)
    	Map<String, String> attributes = Map.of("switchType", "test", "layout", "");
        AddNewProductInputData input = new AddNewProductInputData(
            "Dell XPS", "desc", 1500, 10, "img", 1, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Test OutputData T3)
        AddNewProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Thuộc tính Keyboard không hợp lệ: Loại layout không được để trống.", output.message);
        
        // Khẳng định: CSDL KHÔNG bao giờ được gọi
        verify(productRepository, never()).save(any(ProductData.class));
    }
}
