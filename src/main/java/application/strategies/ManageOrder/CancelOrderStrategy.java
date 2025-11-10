package application.strategies.ManageOrder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import application.dtos.ManageCategory.CategoryData;
import application.dtos.ManageOrder.OrderDetailData;
import application.dtos.ManageProduct.ProductData;
import application.factories.ManageProduct.ProductFactory;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageOrder.OrderDetailRepository;
import application.ports.out.ManageProduct.ProductRepository;
import domain.entities.Keyboard;
import domain.entities.Laptop;
import domain.entities.Mouse;
import domain.entities.Order;
import domain.entities.Product;

public class CancelOrderStrategy implements IOrderStatusUpdateStrategy{
	private OrderDetailRepository orderDetailRepo;
    private ProductRepository productRepo;
    private CategoryRepository categoryRepo;
    private ProductFactory productFactory;
	
    public CancelOrderStrategy(OrderDetailRepository odRepo, ProductRepository pRepo, 
            CategoryRepository cRepo, ProductFactory factory) {
		this.orderDetailRepo = odRepo;
		this.productRepo = pRepo;
		this.categoryRepo = cRepo;
		this.productFactory = factory;
	}
	
	@Override
	public void execute(Order order) {
		// 1. Lấy tất cả Chi tiết (T3 DTO)
        List<OrderDetailData> details = orderDetailRepo.findAllByOrderId(order.getId());
        
        // (Tối ưu: Lấy Map Category 1 lần)
        Map<Integer, CategoryData> categoryMap = categoryRepo.findAll().stream()
                .collect(Collectors.toMap(c -> c.id, c -> c));

        for (OrderDetailData detail : details) {
            // 2. Lấy ProductData (T3 DTO)
            // *** SỬA LỖI 1: Phải tìm bằng getProductId() ***
            ProductData productData = productRepo.findById(detail.productId); 
            
            if (productData == null) {
                // (Sản phẩm đã bị xóa? Bỏ qua...)
                continue;
            }
            
            // 3. Tái tạo Entity (T4)
            Product productEntity = productFactory.load(productData, categoryMap.get(productData.categoryId));
            
            // 4. GỌI LOGIC TẦNG 4 (Entity)
            // *** SỬA LỖI 2: Phải truyền getQuantity() ***
            productEntity.addStock(detail.quantity); // <-- Trả hàng về kho
            
            // 5. Chuyển T4 -> T3 DTO
            ProductData dataToUpdate = mapEntityToData(productEntity);
            
            // 6. Lưu CSDL
            ProductData proTest = productRepo.update(dataToUpdate);
            System.out.println(proTest.stockQuantity);
        }
        
        System.out.println("LOGIC: Đã trả hàng về kho cho Đơn hàng ID: " + order.getId());
	}

	private ProductData mapEntityToData(Product product) {
		ProductData data = new ProductData();
        // 1. Gán thuộc tính chung
        data.id = 0; // ID 0 để tạo mới
        data.name = product.getName();
        data.description = product.getDescription();
        data.price = product.getPrice();
        data.stockQuantity = product.getStockQuantity();
        data.imageUrl = product.getImageUrl();
        data.categoryId = product.getCategoryId();
        
        // 2. Gán thuộc tính riêng (dùng instanceof)
        if (product instanceof Laptop) {
            Laptop laptop = (Laptop) product;
            data.cpu = laptop.getCpu();
            data.ram = laptop.getRam();
            data.screenSize = laptop.getScreenSize();
        } else if (product instanceof Mouse) {
            Mouse mouse = (Mouse) product;
            data.connectionType = mouse.getConnectionType();
            data.dpi = mouse.getDpi();
        } else if (product instanceof Keyboard) {
            Keyboard keyboard = (Keyboard) product;
            data.switchType = keyboard.getSwitchType();
            data.layout = keyboard.getLayout();
        }
        
        return data;
	}

}
