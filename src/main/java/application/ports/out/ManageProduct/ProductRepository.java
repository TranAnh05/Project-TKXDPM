package application.ports.out.ManageProduct;

import java.util.List;

import application.dtos.ManageProduct.ProductData;
import domain.entities.Product;

public interface ProductRepository {
	ProductData findByName(String name);
    ProductData findById(int id);
    List<ProductData> findAll();
    
    // UseCase (T3) sẽ gửi Entity (T4)
    // Repository (T1) sẽ trả về DTO (T3)
    ProductData save(ProductData product);
    ProductData update(ProductData product);
    
    void deleteById(int id);
    int countByCategoryId(int categoryId);
}
