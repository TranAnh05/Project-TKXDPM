package Product.DeleteProduct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Order.FakeOrderRepository;
import Product.FakeProductRepository;
import adapters.ManageProduct.DeleteProduct.DeleteProductPresenter;
import adapters.ManageProduct.DeleteProduct.DeleteProductViewModel;
import adapters.ManageProduct.UpdateProduct.UpdateProductPresenter;
import adapters.ManageProduct.UpdateProduct.UpdateProductViewModel;
import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageProduct.ProductData;
import usecase.ManageProduct.DeleteProduct.DeleteProductInputData;
import usecase.ManageProduct.DeleteProduct.DeleteProductOutputBoundary;
import usecase.ManageProduct.DeleteProduct.DeleteProductOutputData;
import usecase.ManageProduct.DeleteProduct.DeleteProductUsecase;

@ExtendWith(MockitoExtension.class)
public class TestDeleteProductUseCase {
	// 1. "Giả lập" (Mock) các Dependencies
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DeleteProductOutputBoundary productPresenter;
    
    // 2. "Tiêm" (Inject) các Mock vào Interactor
    @InjectMocks
    private DeleteProductUsecase useCase; // (Class T3 của bạn)
    
    /**
     * Test Kịch bản 1: THÀNH CÔNG
     */
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange (Sắp xếp)
        DeleteProductInputData input = new DeleteProductInputData(1);
        
        ProductData mockProduct = new ProductData();
        mockProduct.id = 1;
        mockProduct.name = "Laptop Test";

        // "Dạy" (Stub) Mocks:
        when(productRepository.findById(1)).thenReturn(mockProduct);
        when(orderRepository.isProductInAnyOrder(1)).thenReturn(false); // (KHÔNG có trong đơn hàng)

        // 2. Act (Hành động)
        useCase.execute(input);

        // 3. Assert (Khẳng định)
        // (Test Tầng 3 - Chỉ test OutputData)
        DeleteProductOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals("Đã xóa thành công sản phẩm: Laptop Test", output.message);
        
        // (Quan trọng) Kiểm tra xem CSDL (Mock) có được gọi 'delete' đúng 1 lần không
        verify(productRepository, times(1)).deleteById(1);
    }
    
    /**
     * Test Kịch bản 2: THẤT BẠI - Không tìm thấy id
     */
    @Test
    public void testExecute_Fail_NotFound() {
        // 1. Arrange
        DeleteProductInputData input = new DeleteProductInputData(99);
        
        // Dạy: "Khi findById(99) được gọi, HÃY trả về null"
        when(productRepository.findById(99)).thenReturn(null);

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        DeleteProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Không tìm thấy sản phẩm để xóa.", output.message);
        
        // Khẳng định: 'delete' KHÔNG bao giờ được gọi
        verify(productRepository, never()).deleteById(anyInt());
    }
    
    /**
     * Test Kịch bản 3: THẤT BẠI - Nghiệp vụ (Còn trong Đơn hàng)
     */
    @Test
    public void testExecute_Fail_Business_ProductInUse() {
        // 1. Arrange
        DeleteProductInputData input = new DeleteProductInputData(1);
        
        ProductData mockProduct = new ProductData();
        mockProduct.id = 1;
        mockProduct.name = "Laptop Test";

        // "Dạy" Mocks:
        when(productRepository.findById(1)).thenReturn(mockProduct);
        when(orderRepository.isProductInAnyOrder(1)).thenReturn(true); // (ĐANG CÓ trong đơn hàng)

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        DeleteProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Không thể xóa. Sản phẩm này đang nằm trong một hoặc nhiều đơn hàng.", output.message);
        
        // Khẳng định: 'delete' KHÔNG bao giờ được gọi
        verify(productRepository, never()).deleteById(anyInt());
    }
    
    /**
     * Test Kịch bản 4: THẤT BẠI - Lỗi Hệ thống (CSDL)
     */
    @Test
    public void testExecute_Fail_System_DatabaseError() {
        // 1. Arrange
        DeleteProductInputData input = new DeleteProductInputData(1);
        
        // Dạy: "Khi findById(1) HÃY NÉM LỖI HỆ THỐNG"
        when(productRepository.findById(1)).thenThrow(new RuntimeException("CSDL sập!"));

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        DeleteProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Đã xảy ra lỗi hệ thống. Vui lòng thử lại.", output.message);
    }
}
