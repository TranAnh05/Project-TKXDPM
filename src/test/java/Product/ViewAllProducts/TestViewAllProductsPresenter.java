package Product.ViewAllProducts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import adapters.ManageProduct.ViewAllProducts.ViewAllProductsPresenter;
import adapters.ManageProduct.ViewAllProducts.ViewAllProductsViewModel;
import application.dtos.ManageProduct.ProductOutputData;
import application.dtos.ManageProduct.ViewAllProducts.ViewAllProductsOutputData;

public class TestViewAllProductsPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        ViewAllProductsViewModel viewModel = new ViewAllProductsViewModel();
        ViewAllProductsPresenter presenter = new ViewAllProductsPresenter(viewModel);
        ViewAllProductsOutputData output = new ViewAllProductsOutputData();
        output.success = true;
        
        ProductOutputData pData = new ProductOutputData();
        pData.id = 5; pData.price = 100.5; pData.ram = 16;
        output.products = List.of(pData);
        
        // 2. Act
        presenter.present(output);
        
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success);
        assertEquals("5", viewModel.products.get(0).id);
        assertEquals("100.5", viewModel.products.get(0).price);
        assertEquals("16", viewModel.products.get(0).ram);
    }
}
