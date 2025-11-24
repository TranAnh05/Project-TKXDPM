package Order.ViewOrderDetail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageOrder.ViewOrderDetail.OrderItemViewDTO;
import cgx.com.adapters.ManageOrder.ViewOrderDetail.ViewOrderDetailPresenter;
import cgx.com.adapters.ManageOrder.ViewOrderDetail.ViewOrderDetailViewModel;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.OrderItemData;
import cgx.com.usecase.ManageOrder.ViewOrderDetail.ViewOrderDetailResponseData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ViewOrderDetailPresenterTest {
    
    private ViewOrderDetailPresenter presenter;
    private ViewOrderDetailViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new ViewOrderDetailViewModel();
        presenter = new ViewOrderDetailPresenter(viewModel);
    }
    
    @Test
    void test_present_success() {
        // Mock Item Data
        OrderItemData item1 = new OrderItemData("d1", "Laptop", "img", new BigDecimal("1000"), 2);
        
        OrderData order = new OrderData();
        order.id = "ord-1"; 
        order.totalAmount = new BigDecimal("2000");
        order.items = List.of(item1);

        ViewOrderDetailResponseData response = new ViewOrderDetailResponseData();
        response.success = true;
        response.message = "OK";
        response.order = order;
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertNotNull(viewModel.orderDetail);
        assertEquals("ord-1", viewModel.orderDetail.orderId);
        assertEquals("2000", viewModel.orderDetail.totalAmount);
        
        assertEquals(1, viewModel.orderDetail.items.size());
        OrderItemViewDTO viewItem = viewModel.orderDetail.items.get(0);
        assertEquals("Laptop", viewItem.deviceName);
        assertEquals("1000", viewItem.unitPrice);
        assertEquals("2", viewItem.quantity);
        assertEquals("2000", viewItem.subTotal); // Check logic tính subtotal
    }
    
    @Test
    void test_present_failure() {
        ViewOrderDetailResponseData response = new ViewOrderDetailResponseData();
        response.success = false;
        response.message = "Error";
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertNull(viewModel.orderDetail);
    }
}
