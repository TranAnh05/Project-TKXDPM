package application.ports.out.ManageOrder;

public interface OrderRepository {
	// Dùng cho DeleteProductUseCase
    boolean isProductInAnyOrder(int productId);
}
