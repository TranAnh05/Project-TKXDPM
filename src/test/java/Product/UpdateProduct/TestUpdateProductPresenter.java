package Product.UpdateProduct;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.ManageProduct.UpdateProduct.UpdateProductPresenter;
import adapters.ManageProduct.UpdateProduct.UpdateProductViewModel;
import application.dtos.ManageProduct.ProductOutputData;
import application.dtos.ManageProduct.UpdateProduct.UpdateProductOutputData;

public class TestUpdateProductPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        UpdateProductViewModel viewModel = new UpdateProductViewModel();
        UpdateProductPresenter presenter = new UpdateProductPresenter(viewModel);
        UpdateProductOutputData output = new UpdateProductOutputData();
        output.success = true;
        output.updatedProduct = new ProductOutputData();
        output.updatedProduct.id = 1;
        output.updatedProduct.price = 150.0;
        // 2. Act
        presenter.present(output);
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success);
        assertEquals("1", viewModel.updatedProduct.id);
        assertEquals("150.0", viewModel.updatedProduct.price);
    }
}
