package Order.ViewAllOrders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import Entities.OrderStatus;
import adapters.ManageOrder.ViewAllOrders.ViewAllOrdersPresenter;
import adapters.ManageOrder.ViewAllOrders.ViewAllOrdersViewModel;
import application.dtos.ManageOrder.OrderOutputData;
import application.dtos.ManageOrder.ViewAllOrders.ViewAllOrdersOutputData;

public class TestViewAllOrdersPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        ViewAllOrdersViewModel viewModel = new ViewAllOrdersViewModel();
        ViewAllOrdersPresenter presenter = new ViewAllOrdersPresenter(viewModel);
        ViewAllOrdersOutputData output = new ViewAllOrdersOutputData();
        output.success = true;
        
        OrderOutputData oData = new OrderOutputData();
        oData.totalAmount = 150.75; // <-- double
        oData.status = OrderStatus.SHIPPED; // <-- Enum
        oData.orderDate = LocalDateTime.of(2025, 1, 10, 14, 30); // <-- Date
        output.orders = List.of(oData);
        
        // 2. Act
        presenter.present(output);
        
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success);
        assertEquals("150.75", viewModel.orders.get(0).totalAmount);
        assertEquals("SHIPPED", viewModel.orders.get(0).status);
        assertEquals("14:30 10-01-2025", viewModel.orders.get(0).orderDate);
    }
}
