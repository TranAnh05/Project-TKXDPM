package adapters.ManageProduct.AddNewProduct;

import java.text.DecimalFormat;

import usecase.ManageProduct.ProductOutputData;
import usecase.ManageProduct.AddNewProduct.AddNewProductOutputBoundary;
import usecase.ManageProduct.AddNewProduct.AddNewProductOutputData;

public class AddNewProductPresenter implements AddNewProductOutputBoundary{
	private AddNewProductViewModel viewModel;
	
	private AddNewProductPresenter() {
		
	}
	
    public AddNewProductPresenter(AddNewProductViewModel viewModel) { 
    	this.viewModel = viewModel; 
    }
    
    public AddNewProductViewModel getViewModel() {
    	return viewModel;
    }
    
	@Override
	public void present(AddNewProductOutputData output) {
		ProductViewDTO viewDTO = null;
        if (output.newProduct != null) {
            viewDTO = mapToViewDTO(output.newProduct);
        }
        
        viewModel.message = output.message;
        viewModel.success = String.valueOf(output.success);
        viewModel.newProduct = viewDTO;
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
        
        // Chuyển thuộc tính Laptop
        dto.cpu = data.cpu;
        dto.ram = (data.ram > 0) ? String.valueOf(data.ram) : null;
        dto.screenSize = data.screenSize;
        
        // Chuyển thuộc tính Mouse
        dto.connectionType = data.connectionType;
        dto.dpi = (data.dpi > 0) ? String.valueOf(data.dpi) : null;
        
        // CHUYỂN THUỘC TÍNH MỚI
        dto.switchType = data.switchType;
        dto.layout = data.layout;
        return dto;
	}

}
