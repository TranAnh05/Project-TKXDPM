package SearchProducts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.SearchProducts.SearchProductsPresenter;
import adapters.SearchProducts.SearchProductsViewModel;
import application.dtos.SearchProducts.SearchProductsOutputData;

public class TestSearchProductsPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        SearchProductsViewModel viewModel = new SearchProductsViewModel();
        SearchProductsPresenter presenter = new SearchProductsPresenter(viewModel);
        SearchProductsOutputData output = new SearchProductsOutputData();
        output.success = true;
        // 2. Act
        presenter.present(output);
        // 3. Assert
        assertEquals("true", viewModel.success);
    }
}
