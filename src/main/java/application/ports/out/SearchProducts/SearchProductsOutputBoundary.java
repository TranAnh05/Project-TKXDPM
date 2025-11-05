	package application.ports.out.SearchProducts;

import application.dtos.SearchProducts.SearchProductsOutputData;

public interface SearchProductsOutputBoundary {
	void present(SearchProductsOutputData output);
}
