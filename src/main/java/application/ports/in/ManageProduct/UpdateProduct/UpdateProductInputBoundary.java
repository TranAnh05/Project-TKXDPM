package application.ports.in.ManageProduct.UpdateProduct;

import application.dtos.ManageProduct.UpdateProduct.UpdateProductInputData;

public interface UpdateProductInputBoundary {
	void execute(UpdateProductInputData input);
}
