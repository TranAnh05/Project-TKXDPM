package Product.DeleteProduct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Order.FakeOrderRepository;
import Product.FakeProductRepository;
import adapters.ManageProduct.DeleteProduct.DeleteProductPresenter;
import adapters.ManageProduct.DeleteProduct.DeleteProductViewModel;
import adapters.ManageProduct.UpdateProduct.UpdateProductPresenter;
import adapters.ManageProduct.UpdateProduct.UpdateProductViewModel;
import application.dtos.ManageProduct.ProductData;
import application.dtos.ManageProduct.DeleteProduct.DeleteProductInputData;
import application.dtos.ManageProduct.DeleteProduct.DeleteProductOutputData;
import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageProduct.ProductRepository;
import application.usecases.ManageProduct.DeleteProduct.DeleteProductUsecase;

public class TestDeleteProductUseCase {
	private DeleteProductUsecase useCase;
    private ProductRepository productRepo;
    private OrderRepository orderRepo; 
    private DeleteProductViewModel viewModel;
    private DeleteProductPresenter presenter;
    private ProductData existingProduct; 
    
    @BeforeEach
    public void setup() {
        productRepo = new FakeProductRepository();
        orderRepo = new FakeOrderRepository();
        viewModel = new DeleteProductViewModel();
        presenter = new DeleteProductPresenter(viewModel);
        
        useCase = new DeleteProductUsecase(productRepo, orderRepo, presenter);
        
        // Dữ liệu mồi
        ProductData pData = new ProductData();
        pData.name = "Dell XPS";
        existingProduct = productRepo.save(pData); // ID: 1
    }
    
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange
        DeleteProductInputData input = new DeleteProductInputData(existingProduct.id);
        
        // 2. Act
        useCase.execute(input);

        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        DeleteProductOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        assertEquals("Đã xóa thành công sản phẩm: Dell XPS", output.message);
        
        // Kiểm tra CSDL giả
        assertNull(productRepo.findById(existingProduct.id));
    }
    
    @Test
    public void testExecute_Fail_NotFound() {
        DeleteProductInputData input = new DeleteProductInputData(99);
        useCase.execute(input);
        DeleteProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Không tìm thấy sản phẩm để xóa.", output.message);
    }

    @Test
    public void testExecute_Fail_Business_ProductInUse() {
        // 1. Arrange: "Đánh dấu" sản phẩm (ID: 1) là đang được sử dụng
        ((FakeOrderRepository)orderRepo).setProductInUse(existingProduct.id);
        
        DeleteProductInputData input = new DeleteProductInputData(existingProduct.id);
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        DeleteProductOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Không thể xóa. Sản phẩm này đang nằm trong một hoặc nhiều đơn hàng.", output.message);
        
        // Kiểm tra CSDL giả (KHÔNG bị xóa)
        assertNotNull(productRepo.findById(existingProduct.id));
    }
}
