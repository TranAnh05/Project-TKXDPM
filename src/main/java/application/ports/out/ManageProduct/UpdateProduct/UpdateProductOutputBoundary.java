package application.ports.out.ManageProduct.UpdateProduct;

import application.dtos.ManageProduct.UpdateProduct.UpdateProductOutputData;

public interface UpdateProductOutputBoundary {
	void present(UpdateProductOutputData output);
}
