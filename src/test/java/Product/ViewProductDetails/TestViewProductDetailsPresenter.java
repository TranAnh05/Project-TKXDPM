package Product.ViewProductDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.ManageProduct.ViewProductDetails.ViewProductDetailsPresenter;
import adapters.ManageProduct.ViewProductDetails.ViewProductDetailsViewModel;
import usecase.ManageProduct.ProductOutputData;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsOutputData;

public class TestViewProductDetailsPresenter {
	@Test
    public void testPresent_Conversion() {
        ViewProductDetailsViewModel viewModel = new ViewProductDetailsViewModel();
        ViewProductDetailsPresenter presenter = new ViewProductDetailsPresenter(viewModel);
        ViewProductDetailsOutputData output = new ViewProductDetailsOutputData();
        output.success = true;
        output.product = new ProductOutputData();
        output.product.id = 1;
        output.product.price = 100.0;
        
        presenter.present(output);
        
        assertEquals("true", viewModel.success);
        assertEquals("1", viewModel.product.id);
        assertEquals("100", viewModel.product.price);
    }
}
