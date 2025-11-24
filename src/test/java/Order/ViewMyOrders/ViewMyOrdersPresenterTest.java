package Order.ViewMyOrders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageOrder.ViewMyOrders.OrderSummaryViewDTO;
import cgx.com.adapters.ManageOrder.ViewMyOrders.ViewMyOrdersPresenter;
import cgx.com.adapters.ManageOrder.ViewMyOrders.ViewMyOrdersViewModel;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageOrder.ViewMyOrders.ViewMyOrdersResponseData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ViewMyOrdersPresenterTest {
    
    private ViewMyOrdersPresenter presenter;
    private ViewMyOrdersViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new ViewMyOrdersViewModel();
        presenter = new ViewMyOrdersPresenter(viewModel);
    }
    
    @Test
    void test_present_success() {
        OrderData d1 = new OrderData();
        d1.id = "ord-1"; d1.totalAmount = BigDecimal.TEN; d1.status = "PENDING"; 
        d1.createdAt = Instant.parse("2023-01-01T10:00:00Z");
        d1.items = List.of(new OrderItemData(), new OrderItemData()); // 2 items

        ViewMyOrdersResponseData response = new ViewMyOrdersResponseData();
        response.success = true;
        response.message = "OK";
        response.orders = List.of(d1);
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertEquals(1, viewModel.orders.size());
        
        OrderSummaryViewDTO dto = viewModel.orders.get(0);
        assertEquals("ord-1", dto.orderId);
        assertEquals("10", dto.totalAmount);
        assertEquals("PENDING", dto.status);
        assertEquals("2023-01-01T10:00:00Z", dto.createdAt);
        assertEquals(2, dto.itemCount);
    }
    
    @Test
    void test_present_failure() {
        ViewMyOrdersResponseData response = new ViewMyOrdersResponseData();
        response.success = false;
        response.message = "Error";
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertTrue(viewModel.orders.isEmpty());
    }
}