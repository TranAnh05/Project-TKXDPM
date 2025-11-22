package Product.AddNewProduct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageProduct.AddNewProduct.AddDevicePresenter;
import cgx.com.adapters.ManageProduct.AddNewProduct.AddDeviceViewModel;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddDeviceResponseData;

import static org.junit.jupiter.api.Assertions.*;

public class AddDevicePresenterTest {
    
    private AddDevicePresenter presenter;
    private AddDeviceViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new AddDeviceViewModel();
        presenter = new AddDevicePresenter(viewModel);
    }
    
    @Test
    void test_present_success() {
        AddDeviceResponseData response = new AddDeviceResponseData();
        response.success = true;
        response.message = "OK";
        response.newDeviceId = "dev-123";
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertEquals("OK", viewModel.message);
        assertNotNull(viewModel.newDevice);
        assertEquals("dev-123", viewModel.newDevice.id);
    }
    
    @Test
    void test_present_failure() {
        AddDeviceResponseData response = new AddDeviceResponseData();
        response.success = false;
        response.message = "Error";
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertEquals("Error", viewModel.message);
        assertNull(viewModel.newDevice);
    }
}