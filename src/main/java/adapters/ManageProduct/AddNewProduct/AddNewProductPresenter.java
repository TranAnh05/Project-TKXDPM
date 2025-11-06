package adapters.ManageProduct.AddNewProduct;

import application.dtos.ManageProduct.ProductOutputData;
import application.dtos.ManageProduct.AddNewProduct.AddNewProductOutputData;
import application.ports.out.ManageProduct.AddNewProduct.AddNewProductOutputBoundary;

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
        dto.id = String.valueOf(data.id);
        dto.name = data.name;
        dto.description = data.description;
        dto.price = String.valueOf(data.price);
        dto.stockQuantity = String.valueOf(data.stockQuantity);
        dto.imageUrl = data.imageUrl;
        dto.categoryId = String.valueOf(data.categoryId);
        dto.categoryName = data.categoryName;
        
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
