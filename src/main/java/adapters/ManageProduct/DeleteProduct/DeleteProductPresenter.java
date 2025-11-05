package adapters.ManageProduct.DeleteProduct;

import application.dtos.ManageProduct.DeleteProduct.DeleteProductOutputData;
import application.ports.out.ManageProduct.DeleteProduct.DeleteProductOutputBoundary;

public class DeleteProductPresenter implements DeleteProductOutputBoundary{
	private DeleteProductViewModel viewModel;
	
    public DeleteProductPresenter(DeleteProductViewModel viewModel) { this.viewModel = viewModel; }
    
    public DeleteProductViewModel getViewModel() { return this.viewModel; }
    
	@Override
	public void present(DeleteProductOutputData output) {
		viewModel.success = String.valueOf(output.success); 
        viewModel.message = output.message;
	}
}
