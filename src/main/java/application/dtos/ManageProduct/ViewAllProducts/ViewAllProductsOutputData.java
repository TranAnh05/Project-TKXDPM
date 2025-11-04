package application.dtos.ManageProduct.ViewAllProducts;

import java.util.List;

import application.dtos.ManageProduct.ProductOutputData;

public class ViewAllProductsOutputData {
	public boolean success;
    public String message;
    public List<ProductOutputData> products;
}
