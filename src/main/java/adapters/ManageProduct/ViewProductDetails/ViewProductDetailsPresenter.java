package adapters.ManageProduct.ViewProductDetails;

import java.text.DecimalFormat;

import javax.swing.text.NumberFormatter;

import adapters.ManageProduct.AddNewProduct.ProductViewDTO;
import usecase.ManageProduct.ProductOutputData;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsOutputBoundary;
import usecase.ManageProduct.ViewProductDetails.ViewProductDetailsOutputData;

public class ViewProductDetailsPresenter implements ViewProductDetailsOutputBoundary{
	private ViewProductDetailsViewModel viewModel;
	
	public ViewProductDetailsPresenter(ViewProductDetailsViewModel viewModel) { 
        this.viewModel = viewModel; 
    }
	
	public ViewProductDetailsViewModel getViewModel() { return this.viewModel; }
	
	@Override
	public void present(ViewProductDetailsOutputData output) {
		ProductViewDTO viewDTO = null;
        if (output.product != null) {
            viewDTO = mapToViewDTO(output.product);
        }
        viewModel.success = String.valueOf(output.success);
        viewModel.message = output.message;
        viewModel.product = viewDTO;
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
        
        // Thuộc tính riêng
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
