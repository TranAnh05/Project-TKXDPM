package Order.ViewAllOrders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Order.FakeOrderRepository;
import User.FakeUserRepository;
import application.dtos.ManageOrder.OrderData;
import application.dtos.ManageOrder.OrderOutputData;
import application.dtos.ManageOrder.ViewAllOrders.ViewAllOrdersOutputData;
import application.dtos.ManageUser.UserData;
import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageUser.UserRepository;
import application.usecases.ManageOrder.ViewAllOrders.ViewAllOrdersUsecase;
import domain.entities.OrderStatus;
import domain.entities.Role;

public class TestViewAllOrdersUseCase {
	private ViewAllOrdersUsecase useCase;
    private OrderRepository orderRepo;
    private UserRepository userRepo;
    private UserData customerUser;
    
    @BeforeEach
    public void setup() {
        orderRepo = new FakeOrderRepository();
        userRepo = new FakeUserRepository();
        useCase = new ViewAllOrdersUsecase(orderRepo, userRepo, null);
        
        // Dữ liệu mồi
        UserData cust = new UserData(0, "cust@test.com", "hash", "Cust", "Addr", Role.CUSTOMER, false);
        customerUser = userRepo.save(cust); // ID: 1
    }
    
    @Test
    public void testExecute_SuccessCase_WithData() {
        // 1. Arrange
        OrderData order = new OrderData(0, customerUser.id, LocalDateTime.now(), 1500.0, OrderStatus.PENDING);
        ((FakeOrderRepository)orderRepo).save(order);

        // 2. Act
        useCase.execute();

        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        ViewAllOrdersOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertNull(output.message);
        assertEquals(1, output.orders.size());
        
        // 4. Kiểm tra logic "làm giàu" (Enrichment)
        OrderOutputData result = output.orders.get(0);
        assertEquals(1500.0, result.totalAmount);
        assertEquals(OrderStatus.PENDING, result.status); // <-- Enum
        assertEquals(customerUser.email, result.userEmail); // <-- Email
    }
    
    @Test
    public void testExecute_SuccessCase_NoData() {
        useCase.execute();
        ViewAllOrdersOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        assertEquals("Chưa có đơn hàng nào.", output.message);
        assertEquals(0, output.orders.size());
    }
}
