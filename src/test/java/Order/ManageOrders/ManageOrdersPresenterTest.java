package Order.ManageOrders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageOrder.ManageOrders.ManageOrdersPresenter;
import cgx.com.adapters.ManageOrder.ManageOrders.ManageOrdersViewModel;
import cgx.com.usecase.ManageOrder.OrderData;
import cgx.com.usecase.ManageOrder.ManageOrders.ManageOrdersResponseData;
import cgx.com.usecase.ManageUser.SearchUsers.PaginationData;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ManageOrdersPresenterTest {
    
    private ManageOrdersPresenter presenter;
    private ManageOrdersViewModel viewModel;
    
    @BeforeEach
    void setUp() {
        viewModel = new ManageOrdersViewModel();
        presenter = new ManageOrdersPresenter(viewModel);
    }
    
    @Test
    void test_present_success() {
        OrderData d1 = new OrderData();
        d1.id = "o1"; d1.userId = "u1"; d1.status = "PENDING";
        
        ManageOrdersResponseData response = new ManageOrdersResponseData();
        response.success = true;
        response.message = "OK";
        response.orders = List.of(d1);
        response.pagination = new PaginationData(1, 1, 10);
        
        presenter.present(response);
        
        assertEquals("true", viewModel.success);
        assertEquals(1, viewModel.orders.size());
        assertEquals("u1", viewModel.orders.get(0).userId);
        assertNotNull(viewModel.pagination);
        assertEquals("1", viewModel.pagination.totalCount);
    }
    
    @Test
    void test_present_failure() {
        ManageOrdersResponseData response = new ManageOrdersResponseData();
        response.success = false;
        response.message = "Error";
        
        presenter.present(response);
        
        assertEquals("false", viewModel.success);
        assertTrue(viewModel.orders.isEmpty());
        assertNull(viewModel.pagination);
    }
}