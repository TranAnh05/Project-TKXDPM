package application.factories.ManageOrder;

import Entities.OrderStatus;
import application.factories.ManageProduct.ProductFactory;
import application.ports.out.ManageOrder.OrderDetailRepository;
import application.ports.out.ManageProduct.ProductRepository;
import application.strategies.ManageOrder.CancelOrderStrategy;
import application.strategies.ManageOrder.DefaultStatusStrategy;
import application.strategies.ManageOrder.IOrderStatusUpdateStrategy;
import usecase.ManageCategory.CategoryRepository;

public class StatusStrategyFactory {
	private OrderDetailRepository orderDetailRepo;
    private ProductRepository productRepo;
    private CategoryRepository categoryRepo;
    private ProductFactory productFactory;
    
    public StatusStrategyFactory(OrderDetailRepository odRepo, ProductRepository pRepo, 
            CategoryRepository cRepo, ProductFactory pFactory) {
		this.orderDetailRepo = odRepo;
		this.productRepo = pRepo;
		this.categoryRepo = cRepo;
		this.productFactory = pFactory;
	}
    
    /**
     * Hàm này được gọi bởi Usecase.
     */
    public IOrderStatusUpdateStrategy getStrategy(OrderStatus status) {
        switch (status) {
            case CANCELLED:
                // Trả về Strategy "Trả hàng"
                return new CancelOrderStrategy(
                    orderDetailRepo, productRepo, categoryRepo, productFactory
                );
                
            case SHIPPED:
                // TODO: Trả về Strategy "Gửi Email" (sẽ làm sau)
                // return new ShipOrderStrategy(...);
                return new DefaultStatusStrategy(); // (Tạm thời)

            default:
                // (PENDING, PROCESSING, DELIVERED...)
                return new DefaultStatusStrategy();
        }
    }
}
