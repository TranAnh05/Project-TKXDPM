package application.ports.out.ManageProduct.AddNewProduct;

import application.dtos.ManageProduct.AddNewProduct.AddNewProductOutputData;

public interface AddNewProductOutputBoundary {
	void present(AddNewProductOutputData output);
}
