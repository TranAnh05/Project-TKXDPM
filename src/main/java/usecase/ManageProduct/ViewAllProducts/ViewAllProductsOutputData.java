package usecase.ManageProduct.ViewAllProducts;

import java.util.List;

import usecase.ManageProduct.ProductOutputData;

public class ViewAllProductsOutputData {
	public boolean success;
    public String message;
    public List<ProductSummaryOutputData> products;
}
