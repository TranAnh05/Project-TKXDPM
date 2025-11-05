package SearchOrders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.SearchOrders.SearchOrdersPresenter;
import adapters.SearchOrders.SearchOrdersViewModel;
import application.dtos.SearchOrders.SearchOrdersOutputData;

public class TestSearchOrdersPresenter {
	@Test
    public void testPresent_Conversion() {
        SearchOrdersViewModel viewModel = new SearchOrdersViewModel();
        SearchOrdersPresenter presenter = new SearchOrdersPresenter(viewModel);
        SearchOrdersOutputData output = new SearchOrdersOutputData();
        output.success = true;
        
        presenter.present(output);
        
        assertEquals("true", viewModel.success);
    }
}
