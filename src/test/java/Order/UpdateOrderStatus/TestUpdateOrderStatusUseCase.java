package Order.UpdateOrderStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Category.FakeCategoryRepository;
import Order.FakeOrderRepository;
import OrderDetail.FakeOrderDetailRepository;
import Product.FakeProductRepository;
import User.FakeUserRepository;
import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageOrder.OrderData;
import application.dtos.ManageOrder.OrderDetailData;
import application.dtos.ManageOrder.UpdateOrderStatus.UpdateOrderStatusInputData;
import application.dtos.ManageOrder.UpdateOrderStatus.UpdateOrderStatusOutputData;
import application.dtos.ManageProduct.ProductData;
import application.dtos.ManageUser.UserData;
import application.factories.ManageProduct.ProductFactory;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageOrder.OrderDetailRepository;
import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageProduct.ProductRepository;
import application.ports.out.ManageUser.UserRepository;
import application.usecases.ManageOrder.UpdateOrderStatus.UpdateOrderStatusUsecase;
import domain.entities.OrderStatus;
import domain.entities.Role;

public class TestUpdateOrderStatusUseCase {
	private UpdateOrderStatusUsecase useCase;
    private OrderRepository orderRepo;
    private UserRepository userRepo; 
    private OrderData pendingOrder;
    
    private OrderDetailRepository orderDetailRepo; // <-- Mới
    private ProductRepository productRepo; // <-- Mới
    private CategoryRepository categoryRepo; // <-- Mới
    private ProductFactory productFactory; // <-- Mới
    private ProductData productInOrder;

    @BeforeEach
    public void setup() {
    	// 1. Khởi tạo tất cả Fakes
        orderRepo = new FakeOrderRepository();
        userRepo = new FakeUserRepository();
        orderDetailRepo = new FakeOrderDetailRepository();
        productRepo = new FakeProductRepository();
        categoryRepo = new FakeCategoryRepository();
        productFactory = new ProductFactory();
        
        // 2. Tiêm (Inject) tất cả vào Interactor
        useCase = new UpdateOrderStatusUsecase(
            orderRepo, userRepo, orderDetailRepo, productRepo, 
            categoryRepo, productFactory, null
        );
        
        // 3. Dữ liệu mồi
        UserData cust = userRepo.save(new UserData(0, "cust@test.com", "hash", "Cust", "Addr", Role.CUSTOMER, false));
        CategoryData cat = categoryRepo.save(new CategoryData(0, "Laptop", "{}"));
        
        // (Sản phẩm 1, tồn kho = 10)
        ProductData pData = new ProductData(); 
        pData.name = "Laptop"; pData.categoryId = cat.id; pData.stockQuantity = 10;
        productInOrder = productRepo.save(pData); // ID: 1
        
        // (Đơn hàng 1, status = PENDING, chứa 3 cái Laptop)
        OrderData order = new OrderData(0, cust.id, LocalDateTime.now(), 1500.0, OrderStatus.PENDING);
        pendingOrder = orderRepo.save(order); // ID: 1
       
        // (Chi tiết Đơn hàng 1)
        OrderDetailData detail = new OrderDetailData(1, pendingOrder.id, productInOrder.id, 3, 500.0);
        ((FakeOrderDetailRepository)orderDetailRepo).save(detail);
        //
    }
    
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange: Cập nhật Đơn hàng (ID 1) sang "SHIPPED"
        UpdateOrderStatusInputData input = new UpdateOrderStatusInputData(pendingOrder.id, "SHIPPED");
        
        // 2. Act
        useCase.execute(input);
        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        UpdateOrderStatusOutputData output =  useCase.getOutputData();
        assertTrue(output.success);
        assertEquals("Cập nhật trạng thái thành công!", output.message);
        assertEquals(OrderStatus.SHIPPED, output.updatedOrder.status);
        
        // Kiểm tra CSDL giả
        assertEquals(OrderStatus.SHIPPED, orderRepo.findById(pendingOrder.id).status);
    }
    
    @Test
    public void testExecute_Fail_Validation_InvalidStatusString() {
        // 1. Arrange: Cập nhật Đơn hàng (ID 1) sang "DONE" (không hợp lệ)
        UpdateOrderStatusInputData input = new UpdateOrderStatusInputData(pendingOrder.id, "DONE");

        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        UpdateOrderStatusOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Trạng thái không hợp lệ.", output.message);
        assertEquals(OrderStatus.PENDING, orderRepo.findById(pendingOrder.id).status); // (Không đổi)
    }
    
    @Test
    public void testExecute_SuccessCase_Cancel_RestocksInventory() {
        // 1. Kiểm tra Tồn kho ban đầu
        assertEquals(10, productRepo.findById(productInOrder.id).stockQuantity);
        
        // 2. Arrange: Hủy (CANCELLED) Đơn hàng 1
        UpdateOrderStatusInputData input = new UpdateOrderStatusInputData(pendingOrder.id, "CANCELLED");
        
        // 3. Act
        useCase.execute(input);
        
        // 4. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        UpdateOrderStatusOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        assertEquals(OrderStatus.CANCELLED, output.updatedOrder.status);
        
        // 5. Assert (KIỂM TRA LOGIC NGHIỆP VỤ PHỤ)
        // (Tồn kho = 10 (cũ) + 3 (từ đơn hàng bị hủy) = 13)
        assertEquals(13, productRepo.findById(productInOrder.id).stockQuantity);
    }
}
