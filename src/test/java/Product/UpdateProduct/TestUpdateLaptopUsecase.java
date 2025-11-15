package Product.UpdateProduct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import usecase.ManageProduct.AddNewProduct.AddNewLaptopUsecase;
import usecase.ManageProduct.AddNewProduct.AddNewProductInputData;
import usecase.ManageProduct.AddNewProduct.AddNewProductOutputBoundary;
import usecase.ManageProduct.AddNewProduct.AddNewProductOutputData;
import usecase.ManageProduct.UpdateProduct.UpdateLaptopUsecase;
import usecase.ManageProduct.UpdateProduct.UpdateMouseUsecase;
import usecase.ManageProduct.UpdateProduct.UpdateProductInputData;
import usecase.ManageProduct.UpdateProduct.UpdateProductOutputBoundary;
import usecase.ManageProduct.UpdateProduct.UpdateProductOutputData;
import usecase.ManageProduct.UpdateProduct.UpdateProductUsecase;
import usecase.ManageProduct.ViewAllProducts.ViewAllProductsOutputData;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsInputData;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsOutputData;

@ExtendWith(MockitoExtension.class)
public class TestUpdateLaptopUsecase {
	// 1. "Giả lập" (Mock) các Dependencies (Port T1 và Port T2)
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private UpdateProductOutputBoundary productPresenter;
    
    // 2. "Tiêm" (Inject) các Mock (ở trên) vào Interactor
    @InjectMocks
    private UpdateLaptopUsecase useCase;
    
    // Trường hợp thành công
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange (Sắp xếp)
        Map<String, String> attributes = Map.of("cpu", "Core i7", "ram", "16", "screenSize", "15.6 inch");
        UpdateProductInputData input = new UpdateProductInputData(
            1, "Dell XPS", "desc", 1500, 10, "img", 1, attributes
        );
        
        // (Dạy Repo trả về DTO T3 "thô")
        CategoryData mockCategoryData = new CategoryData(1, "Laptop");
        ProductData mockProductData = new ProductData(
            1, "Dell", "desc", 100, 10, "img", 1, // Chung
            "i7", 16, "15in", // Laptop
            null, 0, null, null // Mouse/Keyboard
        );
        
        
        when(productRepository.findById(1)).thenReturn(mockProductData);
        when(categoryRepository.findById(1)).thenReturn(mockCategoryData);
        when(productRepository.findByName("Dell XPS")).thenReturn(null);

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
        assertEquals(1, output.updatedProduct.id);
        assertEquals("Dell XPS", output.updatedProduct.name);
        assertEquals("Laptop", output.updatedProduct.categoryName);
    }
    
    // TEST VALIDATE CHUNG
    @Test
    public void testExecute_Fail_Validation_Common() {
        // 1. Arrange (Giá âm)
    	 Map<String, String> attributes = Map.of("cpu", "Core i7", "ram", "16", "screenSize", "15.6 inch");
        UpdateProductInputData input = new UpdateProductInputData(
           1, "Dell XPS", "desc", -100, 10, "img", 1, attributes
        );

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Giá sản phẩm không được âm.", output.message);
        
        verify(productRepository, never()).update(any(ProductData.class));
    }
    
    @Test
    public void testExecute_Fail_Validation_Common02() {
        // 1. Arrange (Tồn kho âm)
    	Map<String, String> attributes = Map.of("cpu", "Core i7", "ram", "16", "screenSize", "15.6 inch");
    	UpdateProductInputData input = new UpdateProductInputData(
            1, "Dell XPS", "desc", 100, -10, "img", 1, attributes
        );

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Số lượng tồn kho không được âm.", output.message);
        
        verify(productRepository, never()).update(any(ProductData.class));
    }
    
    
    @Test
    public void testExecute_Fail_Validation_Common03() {
        // 1. Arrange (tên rỗng)
    	Map<String, String> attributes = Map.of("cpu", "Core i7", "ram", "16", "screenSize", "15.6 inch");
        UpdateProductInputData input = new UpdateProductInputData(
            1, "", "desc", 100, 10, "img", 1, attributes
        );

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Tên sản phẩm không được để trống.", output.message);
        
        verify(productRepository, never()).update(any(ProductData.class));
    }
    
    @Test
    public void testExecute_Fail_Validation_Common04() {
        // 1. Arrange (mô tả rỗng)
        Map<String, String> attributes = Map.of("cpu", "Core i7", "ram", "16");
        UpdateProductInputData input = new UpdateProductInputData(
           1, "test name", "", 100, 10, "img", 1, attributes
        );

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Mô tả sản phẩm không được để trống.", output.message);
        
        verify(productRepository, never()).save(any(ProductData.class));
    }
    
    // TEST VALIDATE RIÊNG
    // trường hợp test thất bại
    @Test
    public void testExecute_Fail_Validation_Specific() {
        // 1. Arrange (RAM âm, vi phạm Tầng 4)
    	Map<String, String> attributes = Map.of("cpu", "Core i7", "ram", "-16", "screenSize", "15.6 inch");
    	UpdateProductInputData input = new UpdateProductInputData(
           1, "Dell XPS", "desc", 1500, 10, "img", 1, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Test OutputData T3)
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Thuộc tính Laptop không hợp lệ: RAM phải là số dương.", output.message);
        
        // Khẳng định: CSDL KHÔNG bao giờ được gọi
        verify(productRepository, never()).update(any(ProductData.class));
    }
    
    @Test
    public void testExecute_Fail_Validation_Specific02() {
        // 1. Arrange (cpu rỗng - vi phạm Tầng 4)
    	Map<String, String> attributes = Map.of("cpu", "", "ram", "16", "screenSize", "15.6 inch");
    	UpdateProductInputData input = new UpdateProductInputData(
           1, "Dell XPS", "desc", 1500, 10, "img", 1, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Test OutputData T3)
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Thuộc tính Laptop không hợp lệ: CPU không được để trống.", output.message);
        
        // Khẳng định: CSDL KHÔNG bao giờ được gọi
        verify(productRepository, never()).save(any(ProductData.class));
    }
    
    @Test
    public void testExecute_Fail_Validation_Specific03() {
        // 1. Arrange (screenSize rỗng - vi phạm Tầng 4)
    	Map<String, String> attributes = Map.of("cpu", "test cpu", "ram", "16", "screenSize", "");
    	UpdateProductInputData input = new UpdateProductInputData(
            1, "Dell XPS", "desc", 1500, 10, "img", 1, attributes
        );
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (Test OutputData T3)
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Thuộc tính Laptop không hợp lệ: Kích thước màn hình không được để trống.", output.message);
        
        // Khẳng định: CSDL KHÔNG bao giờ được gọi
        verify(productRepository, never()).save(any(ProductData.class));
    }
    
    // trường hợp thất bại do lỗi kết nối với csdl
    @Test
    public void testExecute_Fail_DatabaseError() {
        // 1. Arrange (Dạy Mock ném lỗi hệ thống)
    	Map<String, String> attributes = Map.of("cpu", "test cpu", "ram", "16", "screenSize", "test");
    	UpdateProductInputData input = new UpdateProductInputData(
            1, "Dell XPS", "desc", 1500, 10, "img", 1, attributes
        );
    	
        when(productRepository.findById(input.id)).thenThrow(new RuntimeException("CSDL sập"));

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        UpdateProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Đã xảy ra lỗi hệ thống không xác định.", output.message);
    }
}
