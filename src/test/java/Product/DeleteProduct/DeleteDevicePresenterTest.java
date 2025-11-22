package Product.DeleteProduct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageProduct.DeleteDevice.DeleteDevicePresenter;
import cgx.com.adapters.ManageProduct.DeleteDevice.DeleteDeviceViewModel;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteDeviceResponseData;

import static org.junit.jupiter.api.Assertions.*;

public class DeleteDevicePresenterTest {
    
    private DeleteDevicePresenter presenter;
    private DeleteDeviceViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new DeleteDeviceViewModel();
        presenter = new DeleteDevicePresenter(viewModel);
    }
    
    @Test
    void test_present_success() {
        DeleteDeviceResponseData response = new DeleteDeviceResponseData();
        response.success = true;
        response.message = "OK";
        response.deletedDeviceId = "dev-1";
        response.newStatus = "DELETED";
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertEquals("OK", viewModel.message);
        assertEquals("dev-1", viewModel.deletedId);
        assertEquals("DELETED", viewModel.status);
    }
    
    @Test
    void test_present_failure() {
        DeleteDeviceResponseData response = new DeleteDeviceResponseData();
        response.success = false;
        response.message = "Error";
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertEquals("Error", viewModel.message);
        assertNull(viewModel.deletedId);
    }
}
