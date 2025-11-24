package Order.CancelOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageOrder.CancelOrder.CancelOrderPresenter;
import cgx.com.adapters.ManageOrder.CancelOrder.CancelOrderViewModel;
import cgx.com.usecase.ManageOrder.CancelOrder.CancelOrderResponseData;

import static org.junit.jupiter.api.Assertions.*;

public class CancelOrderPresenterTest {
    
    private CancelOrderPresenter presenter;
    private CancelOrderViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new CancelOrderViewModel();
        presenter = new CancelOrderPresenter(viewModel);
    }
    
    @Test
    void test_present_success() {
        CancelOrderResponseData response = new CancelOrderResponseData();
        response.success = true;
        response.message = "OK";
        response.orderId = "123";
        response.status = "CANCELLED";
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertEquals("CANCELLED", viewModel.status);
    }
    
    @Test
    void test_present_failure() {
        CancelOrderResponseData response = new CancelOrderResponseData();
        response.success = false;
        response.message = "Error";
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertEquals("Error", viewModel.message);
        assertNull(viewModel.status);
    }
}