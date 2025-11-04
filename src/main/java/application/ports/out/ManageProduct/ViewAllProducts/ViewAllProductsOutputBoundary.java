package application.ports.out.ManageProduct.ViewAllProducts;

import application.dtos.ManageProduct.ViewAllProducts.ViewAllProductsOutputData;

public interface ViewAllProductsOutputBoundary {
	void present(ViewAllProductsOutputData output);
}
