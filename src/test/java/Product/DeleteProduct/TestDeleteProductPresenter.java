package Product.DeleteProduct;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.ManageProduct.DeleteProduct.DeleteProductPresenter;
import adapters.ManageProduct.DeleteProduct.DeleteProductViewModel;
import usecase.ManageProduct.DeleteProduct.DeleteProductOutputData;

public class TestDeleteProductPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        DeleteProductViewModel viewModel = new DeleteProductViewModel();
        DeleteProductPresenter presenter = new DeleteProductPresenter(viewModel);
        DeleteProductOutputData output = new DeleteProductOutputData();
        output.success = false;
        output.message = "Lỗi";
        // 2. Act
        presenter.present(output);
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("false", viewModel.success);
        assertEquals("Lỗi", viewModel.message);
    }
}
