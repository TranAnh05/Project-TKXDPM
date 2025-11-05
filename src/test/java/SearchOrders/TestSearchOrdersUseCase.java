package SearchOrders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Order.FakeOrderRepository;
import User.FakeUserRepository;
import application.dtos.ManageOrder.OrderData;
import application.dtos.ManageUser.UserData;
import application.dtos.SearchOrders.SearchOrdersInputData;
import application.dtos.SearchOrders.SearchOrdersOutputData;
import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageUser.UserRepository;
import application.usecases.SearchOrders.SearchOrdersUsecase;
import domain.entities.OrderStatus;
import domain.entities.Role;

public class TestSearchOrdersUseCase {
	private SearchOrdersUsecase useCase;
    private OrderRepository orderRepo;
    private UserRepository userRepo;
    
    private UserData user1;
    private UserData user2;
    
    @BeforeEach
    public void setup() {
        orderRepo = new FakeOrderRepository();
        userRepo = new FakeUserRepository();
        useCase = new SearchOrdersUsecase(orderRepo, userRepo, null);
        
        // Dữ liệu mồi
        user1 = userRepo.save(new UserData(0, "user1@test.com", "hash", "U1", "Addr", Role.CUSTOMER, false)); // ID 1
        user2 = userRepo.save(new UserData(0, "user2@test.com", "hash", "U2", "Addr", Role.CUSTOMER, false)); // ID 2
        // User không có đơn hàng
        userRepo.save(new UserData(0, "user3@no-order.com", "hash", "U3", "Addr", Role.CUSTOMER, false)); // ID 3
        
        // User 1 có 2 đơn hàng
        orderRepo.save(new OrderData(0, user1.id, LocalDateTime.now(), 100.0, OrderStatus.PENDING)); // ID 1
        orderRepo.save(new OrderData(0, user1.id, LocalDateTime.now(), 200.0, OrderStatus.SHIPPED)); // ID 2
        // User 2 có 1 đơn hàng
        orderRepo.save(new OrderData(0, user2.id, LocalDateTime.now(), 50.0, OrderStatus.PENDING)); // ID 3
    }
    
    @Test
    public void testExecute_SuccessCase_FoundMultiple() {
        // 1. Arrange: Tìm "@test.com" (sẽ ra user 1 và 2)
        SearchOrdersInputData input = new SearchOrdersInputData("@test.com");
        
        // 2. Act
        useCase.execute(input);

        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        SearchOrdersOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals(3, output.orders.size()); // Tìm thấy 3 đơn hàng
        assertEquals("user1@test.com", output.orders.get(0).userEmail);
        assertEquals("user2@test.com", output.orders.get(2).userEmail);
    }
    
    @Test
    public void testExecute_SuccessCase_FoundOneUser() {
        // 1. Arrange: Tìm "user1" (chỉ ra user 1)
        SearchOrdersInputData input = new SearchOrdersInputData("user1@test.com");
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        SearchOrdersOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        assertEquals(2, output.orders.size()); // Tìm thấy 2 đơn hàng của user 1
    }
    
    @Test
    public void testExecute_Fail_UserFound_NoOrders() {
        // 1. Arrange: Tìm "user3@no-order.com"
        SearchOrdersInputData input = new SearchOrdersInputData("user3@no-order.com");
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        SearchOrdersOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        assertEquals(0, output.orders.size());
        assertTrue(output.message.contains("không có đơn hàng nào"));
    }
    
    @Test
    public void testExecute_Fail_UserNotFound() {
        // 1. Arrange: Tìm "nouser@xxx.com"
        SearchOrdersInputData input = new SearchOrdersInputData("nouser@xxx.com");
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        SearchOrdersOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        assertEquals(0, output.orders.size());
        assertTrue(output.message.contains("Không tìm thấy người dùng"));
    }
}
