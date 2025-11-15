package Product.ViewAllProducts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import adapters.ManageProduct.ViewAllProducts.ViewAllProductsPresenter;
import adapters.ManageProduct.ViewAllProducts.ViewAllProductsViewModel;
import usecase.ManageProduct.ProductOutputData;
import usecase.ManageProduct.ViewAllProducts.ProductSummaryOutputData;
import usecase.ManageProduct.ViewAllProducts.ViewAllProductsOutputData;

public class TestViewAllProductsPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        ViewAllProductsViewModel viewModel = new ViewAllProductsViewModel();
        ViewAllProductsPresenter presenter = new ViewAllProductsPresenter(viewModel);
        ViewAllProductsOutputData output = new ViewAllProductsOutputData();
        output.success = true;
        
        ProductSummaryOutputData pData = new ProductSummaryOutputData();
        pData.id = 5; pData.price = 10000000.0;
        output.products = List.of(pData);
        
        // 2. Act
        presenter.present(output);
        
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success);
        assertEquals("5", viewModel.products.get(0).id);
        assertNull(viewModel.message);
        assertEquals("10000000", viewModel.products.get(0).price);
    }
}
