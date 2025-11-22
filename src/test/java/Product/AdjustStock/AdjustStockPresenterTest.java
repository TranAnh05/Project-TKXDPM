package Product.AdjustStock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageProduct.AdjustStock.AdjustStockPresenter;
import cgx.com.adapters.ManageProduct.AdjustStock.AdjustStockViewModel;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustStockResponseData;

import static org.junit.jupiter.api.Assertions.*;

public class AdjustStockPresenterTest {
    
    private AdjustStockPresenter presenter;
    private AdjustStockViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new AdjustStockViewModel();
        presenter = new AdjustStockPresenter(viewModel);
    }
    
    @Test
    void test_present_success() {
        AdjustStockResponseData response = new AdjustStockResponseData();
        response.success = true;
        response.message = "OK";
        response.currentStock = 100;
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertEquals("OK", viewModel.message);
        assertEquals("100", viewModel.currentStock);
    }
    
    @Test
    void test_present_failure() {
        AdjustStockResponseData response = new AdjustStockResponseData();
        response.success = false;
        response.message = "Error";
        // currentStock mặc định là 0
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertEquals("Error", viewModel.message);
        assertNull(viewModel.currentStock);
    }
}