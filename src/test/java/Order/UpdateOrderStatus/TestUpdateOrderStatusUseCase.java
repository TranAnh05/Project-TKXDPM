package Order.UpdateOrderStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Order.FakeOrderRepository;
import User.FakeUserRepository;
import application.dtos.ManageOrder.OrderData;
import application.dtos.ManageOrder.UpdateOrderStatus.UpdateOrderStatusInputData;
import application.dtos.ManageOrder.UpdateOrderStatus.UpdateOrderStatusOutputData;
import application.dtos.ManageUser.UserData;
import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageUser.UserRepository;
import application.usecases.ManageOrder.UpdateOrderStatus.UpdateOrderStatusUsecase;
import domain.entities.OrderStatus;
import domain.entities.Role;

public class TestUpdateOrderStatusUseCase {
	private UpdateOrderStatusUsecase useCase;
    private OrderRepository orderRepo;
    private UserRepository userRepo; 
    private OrderData pendingOrder;

    @BeforeEach
    public void setup() {
        orderRepo = new FakeOrderRepository();
        userRepo = new FakeUserRepository(); 
        useCase = new UpdateOrderStatusUsecase(orderRepo, userRepo, null);
        
        // Dữ liệu mồi: 1 User, 1 Order (Status PENDING)
        UserData cust = userRepo.save(new UserData(0, "cust@test.com", "hash", "Cust", "Addr", Role.CUSTOMER, false));
        pendingOrder = orderRepo.save(new OrderData(0, cust.id, LocalDateTime.now(), 1500.0, OrderStatus.PENDING));
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
}
