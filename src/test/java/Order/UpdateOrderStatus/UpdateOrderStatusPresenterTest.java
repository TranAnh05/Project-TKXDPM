package Order.UpdateOrderStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageOrder.UpdateOrderStatus.UpdateOrderStatusPresenter;
import cgx.com.adapters.ManageOrder.UpdateOrderStatus.UpdateOrderStatusViewModel;
import cgx.com.usecase.ManageOrder.UpdateOrderStatus.UpdateOrderStatusResponseData;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateOrderStatusPresenterTest {
    
    private UpdateOrderStatusPresenter presenter;
    private UpdateOrderStatusViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new UpdateOrderStatusViewModel();
        presenter = new UpdateOrderStatusPresenter(viewModel);
    }
    
    @Test
    void test_present_success() {
        UpdateOrderStatusResponseData response = new UpdateOrderStatusResponseData();
        response.success = true;
        response.message = "OK";
        response.orderId = "123";
        response.status = "CONFIRMED";
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertEquals("CONFIRMED", viewModel.status);
    }
    
    @Test
    void test_present_failure() {
        UpdateOrderStatusResponseData response = new UpdateOrderStatusResponseData();
        response.success = false;
        response.message = "Error";
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertNull(viewModel.status);
    }
}