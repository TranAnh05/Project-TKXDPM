package application.ports.in.ManageProduct.DeleteProduct;

import application.dtos.ManageProduct.DeleteProduct.DeleteProductInputData;

public interface DeleteProductInputBoundary {
	void execute(DeleteProductInputData input);
}
