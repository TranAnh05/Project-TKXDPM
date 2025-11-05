package application.ports.out.ManageProduct.DeleteProduct;

import application.dtos.ManageProduct.DeleteProduct.DeleteProductOutputData;

public interface DeleteProductOutputBoundary {
	void present(DeleteProductOutputData output);
}
