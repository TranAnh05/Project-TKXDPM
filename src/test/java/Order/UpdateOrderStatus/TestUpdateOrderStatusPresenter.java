package Order.UpdateOrderStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import Entities.OrderStatus;
import adapters.ManageOrder.UpdateOrderStatus.UpdateOrderStatusPresenter;
import adapters.ManageOrder.UpdateOrderStatus.UpdateOrderStatusViewModel;
import application.dtos.ManageOrder.OrderOutputData;
import application.dtos.ManageOrder.UpdateOrderStatus.UpdateOrderStatusOutputData;

public class TestUpdateOrderStatusPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        UpdateOrderStatusViewModel viewModel = new UpdateOrderStatusViewModel();
        UpdateOrderStatusPresenter presenter = new UpdateOrderStatusPresenter(viewModel);
        UpdateOrderStatusOutputData output = new UpdateOrderStatusOutputData();
        output.success = true;
        output.updatedOrder = new OrderOutputData();
        output.updatedOrder.status = OrderStatus.PROCESSING; // <-- Enum
        output.updatedOrder.orderDate = LocalDateTime.now(); // <-- Date
        
        // 2. Act
        presenter.present(output);
        
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success);
        assertEquals("PROCESSING", viewModel.updatedOrder.status); // Enum -> String
        assertNotNull(viewModel.updatedOrder.orderDate); // (Đã được format)
    }
}
