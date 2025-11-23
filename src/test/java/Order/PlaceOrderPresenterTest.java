package Order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageOrder.PlaceOrder.PlaceOrderPresenter;
import cgx.com.adapters.ManageOrder.PlaceOrder.PlaceOrderViewModel;
import cgx.com.usecase.ManageOrder.PlaceOrder.PlaceOrderResponseData;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class PlaceOrderPresenterTest {
    
    private PlaceOrderPresenter presenter;
    private PlaceOrderViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new PlaceOrderViewModel();
        presenter = new PlaceOrderPresenter(viewModel);
    }
    
    @Test
    void test_present_success() {
        PlaceOrderResponseData response = new PlaceOrderResponseData();
        response.success = true;
        response.message = "OK";
        response.orderId = "ord-123";
        response.totalAmount = new BigDecimal("5000.00");
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertEquals("OK", viewModel.message);
        assertNotNull(viewModel.order);
        assertEquals("ord-123", viewModel.order.orderId);
        assertEquals("5000.00", viewModel.order.totalAmount);
    }
    
    @Test
    void test_present_failure() {
        PlaceOrderResponseData response = new PlaceOrderResponseData();
        response.success = false;
        response.message = "Error";
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertEquals("Error", viewModel.message);
        assertNull(viewModel.order);
    }
}
