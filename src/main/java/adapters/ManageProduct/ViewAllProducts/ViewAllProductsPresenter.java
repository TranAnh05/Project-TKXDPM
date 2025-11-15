package adapters.ManageProduct.ViewAllProducts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import adapters.ManageProduct.AddNewProduct.ProductViewDTO;
import usecase.ManageProduct.ProductOutputData;
import usecase.ManageProduct.ViewAllProducts.ProductSummaryOutputData;
import usecase.ManageProduct.ViewAllProducts.ViewAllProductsOutputBoundary;
import usecase.ManageProduct.ViewAllProducts.ViewAllProductsOutputData;

public class ViewAllProductsPresenter implements ViewAllProductsOutputBoundary{
	private ViewAllProductsViewModel viewModel;
	
    public ViewAllProductsPresenter(ViewAllProductsViewModel viewModel) { 
    	this.viewModel = viewModel; 
    }
    
    public ViewAllProductsViewModel getViewModel() { return this.viewModel; }
    
	@Override
	public void present(ViewAllProductsOutputData output) {
		List<ProductSummaryViewItem> viewDTOs = new ArrayList<>();
        if (output.products != null) {
            for (ProductSummaryOutputData productData : output.products) {
                viewDTOs.add(mapToViewDTO(productData));
            }
        }
        
        viewModel.message = output.message;
        viewModel.success = String.valueOf(output.success);
        viewModel.products = viewDTOs;
		
	}

	private ProductSummaryViewItem mapToViewDTO(ProductSummaryOutputData data) {
		ProductSummaryViewItem dto = new ProductSummaryViewItem();
		DecimalFormat numberFormatter = new DecimalFormat("#");
		numberFormatter.setGroupingUsed(false);
		
        dto.id = String.valueOf(data.id);
        dto.name = data.name;
        dto.price = numberFormatter.format(data.price);
        dto.stockQuantity = String.valueOf(data.stockQuantity);
        dto.imageUrl = data.imageUrl;
        dto.categoryName = data.categoryName;
        
        return dto;
	}

	
}
