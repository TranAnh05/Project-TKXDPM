package application.dtos.SearchProducts;

import java.util.List;

import application.dtos.ManageProduct.ProductOutputData;

public class SearchProductsOutputData {
	public boolean success;
    public String message;
    public List<ProductOutputData> products;
}
