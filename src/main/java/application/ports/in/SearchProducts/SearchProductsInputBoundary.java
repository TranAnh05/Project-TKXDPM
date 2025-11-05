package application.ports.in.SearchProducts;

import application.dtos.SearchProducts.SearchProductsInputData;

public interface SearchProductsInputBoundary {
	void execute(SearchProductsInputData input);
}
