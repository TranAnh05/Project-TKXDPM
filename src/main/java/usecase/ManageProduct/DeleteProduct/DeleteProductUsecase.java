package usecase.ManageProduct.DeleteProduct;

import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageProduct.ProductRepository;
import usecase.ManageProduct.ProductData;

public class DeleteProductUsecase implements DeleteProductInputBoundary{
	private ProductRepository productRepository;
    private OrderRepository orderRepository; 
    private DeleteProductOutputBoundary productPresenter;
    private DeleteProductOutputData outputData; 
    
    
    public DeleteProductUsecase() {
    	
    }
    
	public DeleteProductUsecase(ProductRepository productRepository, OrderRepository orderRepository,
			DeleteProductOutputBoundary productPresenter) {
		this.productRepository = productRepository;
		this.orderRepository = orderRepository;
		this.productPresenter = productPresenter;
	}
	
	public DeleteProductOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(DeleteProductInputData input) {
		outputData = new DeleteProductOutputData();
		
		try {
			// 1. Kiểm tra sản phẩm có tồn tại không
            ProductData existingProduct = productRepository.findById(input.productId);
            if (existingProduct == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm để xóa.");
            }
            
            // 2. Kiểm tra nghiệp vụ (Logic cốt lõi)
            if (orderRepository.isProductInAnyOrder(input.productId)) {
                throw new IllegalArgumentException("Không thể xóa. Sản phẩm này đang nằm trong một hoặc nhiều đơn hàng.");
            }
            
            // 3. Xóa (Happy Path)
            productRepository.deleteById(input.productId);
            
            // 4. Báo cáo thành công
            outputData.success = true;
            outputData.message = "Đã xóa thành công sản phẩm: " + existingProduct.name;
		} catch (IllegalArgumentException e) {
            // 5. BẮT LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
        } catch (Exception e) {
        	// 6. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.";
		}
		
		productPresenter.present(outputData);
	}

}
