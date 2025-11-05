package adapters.ManageProduct.UpdateProduct;

import adapters.ManageProduct.AddNewProduct.ProductViewDTO;
import application.dtos.ManageProduct.ProductOutputData;
import application.dtos.ManageProduct.UpdateProduct.UpdateProductOutputData;
import application.ports.out.ManageProduct.UpdateProduct.UpdateProductOutputBoundary;

public class UpdateProductPresenter implements UpdateProductOutputBoundary{
	private UpdateProductViewModel viewModel;
	
	public UpdateProductPresenter() {}
    public UpdateProductPresenter(UpdateProductViewModel viewModel) { this.viewModel = viewModel; }
    
    public UpdateProductViewModel getViewModel() { return this.viewModel; }
    
	@Override
	public void present(UpdateProductOutputData output) {
		ProductViewDTO viewDTO = null;
        if (output.updatedProduct != null) {
            viewDTO = mapToViewDTO(output.updatedProduct);
        }
        
        viewModel.success = String.valueOf(output.success);
        viewModel.message = output.message;
        viewModel.updatedProduct = viewDTO;
	}
	private ProductViewDTO mapToViewDTO(ProductOutputData data) {
		ProductViewDTO dto = new ProductViewDTO();
		// Chung
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
