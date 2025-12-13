package Product.SearchDevices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageProduct.SearchDevices.SearchDevicesPresenter;
import cgx.com.adapters.ManageProduct.SearchDevices.SearchDevicesViewModel;
import cgx.com.usecase.Interface_Common.PaginationData;
import cgx.com.usecase.ManageProduct.DeviceData;
import cgx.com.usecase.ManageProduct.SearchDevices.SearchDevicesResponseData;

import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SearchDevicesPresenterTest {
    
    private SearchDevicesPresenter presenter;
    private SearchDevicesViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new SearchDevicesViewModel();
        presenter = new SearchDevicesPresenter(viewModel);
    }
    
    @Test
    void test_present_success() {
        DeviceData d1 = new DeviceData();
        d1.id = "1"; d1.name = "L1"; d1.price = BigDecimal.TEN;
        
        SearchDevicesResponseData response = new SearchDevicesResponseData();
        response.success = true;
        response.message = "OK";
        response.devices = List.of(d1);
        response.pagination = new PaginationData(1, 1, 10);
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertEquals("OK", viewModel.message);
        assertEquals(1, viewModel.devices.size());
        assertEquals("L1", viewModel.devices.get(0).name);
        assertNotNull(viewModel.pagination);
        assertEquals("1", viewModel.pagination.totalCount);
    }
    
    @Test
    void test_present_failure() {
        SearchDevicesResponseData response = new SearchDevicesResponseData();
        response.success = false;
        response.message = "Error";
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertTrue(viewModel.devices.isEmpty());
        assertNull(viewModel.pagination);
    }
}
