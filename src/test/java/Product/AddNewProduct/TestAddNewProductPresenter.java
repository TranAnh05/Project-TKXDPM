package Product.AddNewProduct;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.ManageProduct.AddNewProduct.AddNewProductPresenter;
import adapters.ManageProduct.AddNewProduct.AddNewProductViewModel;
import usecase.ManageProduct.ProductOutputData;
import usecase.ManageProduct.AddNewProduct.AddNewProductOutputData;

public class TestAddNewProductPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        AddNewProductViewModel viewModel = new AddNewProductViewModel();
        AddNewProductPresenter presenter = new AddNewProductPresenter(viewModel);
        AddNewProductOutputData output = new AddNewProductOutputData();
        output.success = true;
        output.newProduct = new ProductOutputData();
        output.newProduct.id = 5; // <-- int
        output.newProduct.price = 1500.0; // <-- double
        output.newProduct.ram = 16; // <-- int
        
        // 2. Act
        
        presenter.present(output);
        
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        
        assertEquals("true", viewModel.success);
        assertEquals("5", viewModel.newProduct.id);
        assertEquals("1500.0", viewModel.newProduct.price);
        assertEquals("16", viewModel.newProduct.ram);
    }
}
