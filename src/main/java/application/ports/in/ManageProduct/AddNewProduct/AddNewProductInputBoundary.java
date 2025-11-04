package application.ports.in.ManageProduct.AddNewProduct;

import application.dtos.ManageProduct.AddNewProduct.AddNewProductInputData;

public interface AddNewProductInputBoundary {
	void execute(AddNewProductInputData input);
}
