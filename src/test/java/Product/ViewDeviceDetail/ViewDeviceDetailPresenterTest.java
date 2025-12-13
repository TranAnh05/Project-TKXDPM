package Product.ViewDeviceDetail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageProduct.ViewDeviceDetail.ViewDeviceDetailPresenter;
import cgx.com.adapters.ManageProduct.ViewDeviceDetail.ViewDeviceDetailViewModel;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailResponseData;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class ViewDeviceDetailPresenterTest {
    
    private ViewDeviceDetailPresenter presenter;
    private ViewDeviceDetailViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new ViewDeviceDetailViewModel();
        presenter = new ViewDeviceDetailPresenter(viewModel);
    }
    
    @Test
    void test_present_success_laptop() {
        DeviceData laptop = new DeviceData();
        laptop.id = "1";
        laptop.name = "MacBook";
        laptop.price = new BigDecimal("2000");
        laptop.cpu = "M1"; 
        laptop.screenSize = 13.3;
        
        ViewDeviceDetailResponseData response = new ViewDeviceDetailResponseData();
        response.success = true;
        response.message = "OK";
        response.device = laptop;
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertEquals("M1", viewModel.device.cpu);
        assertEquals("13.3", viewModel.device.screenSize);
        assertNull(viewModel.device.dpi); // Mouse spec should be null
    }
    
    @Test
    void test_present_failure() {
        ViewDeviceDetailResponseData response = new ViewDeviceDetailResponseData();
        response.success = false;
        response.message = "Error";
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertNull(viewModel.device);
    }
}
