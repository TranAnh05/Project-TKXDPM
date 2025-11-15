package usecase.ManageProduct.SearchProducts;

import java.util.List;

import usecase.ManageProduct.ProductOutputData;

public class SearchProductsOutputData {
	public boolean success;
    public String message;
    public List<ProductOutputData> products;
}
