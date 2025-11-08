package adapters.ManageProduct.ViewAllProducts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import adapters.ManageProduct.AddNewProduct.ProductViewDTO;
import application.dtos.ManageProduct.ProductOutputData;
import application.dtos.ManageProduct.ViewAllProducts.ViewAllProductsOutputData;
import application.ports.out.ManageProduct.ViewAllProducts.ViewAllProductsOutputBoundary;

public class ViewAllProductsPresenter implements ViewAllProductsOutputBoundary{
	private ViewAllProductsViewModel viewModel;
	
    public ViewAllProductsPresenter(ViewAllProductsViewModel viewModel) { 
    	this.viewModel = viewModel; 
    }
    
    public ViewAllProductsViewModel getViewModel() { return this.viewModel; }
    
	@Override
	public void present(ViewAllProductsOutputData output) {
		List<ProductViewDTO> viewDTOs = new ArrayList<>();
        if (output.products != null) {
            for (ProductOutputData productData : output.products) {
                viewDTOs.add(mapToViewDTO(productData));
            }
        }
        
        viewModel.message = output.message;
        viewModel.success = String.valueOf(output.success);
        viewModel.products = viewDTOs;
		
	}

	private ProductViewDTO mapToViewDTO(ProductOutputData data) {
		ProductViewDTO dto = new ProductViewDTO();
		DecimalFormat numberFormatter = new DecimalFormat("#");
		numberFormatter.setGroupingUsed(false);
		
        dto.id = String.valueOf(data.id);
        dto.name = data.name;
        dto.description = data.description;
        dto.price = numberFormatter.format(data.price);
        dto.stockQuantity = String.valueOf(data.stockQuantity);
        dto.imageUrl = data.imageUrl;
        dto.categoryId = String.valueOf(data.categoryId);
        dto.categoryName = data.categoryName;
        
        // Chuyển thuộc tính riêng (phẳng)
        dto.cpu = data.cpu;
        dto.ram = (data.ram > 0) ? String.valueOf(data.ram) : null;
        dto.screenSize = data.screenSize;
        dto.connectionType = data.connectionType;
        dto.dpi = (data.dpi > 0) ? String.valueOf(data.dpi) : null;
        dto.switchType = data.switchType;
        dto.layout = data.layout;
        return dto;
	}

}
