package Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import application.dtos.ManageProduct.ProductData;
import application.ports.out.ManageProduct.ProductRepository;

public class FakeProductRepository implements ProductRepository{
	private Map<Integer, ProductData> database = new HashMap<>();
    private int sequence = 0;
    
	@Override
	public ProductData findByName(String name) {
		for (ProductData d : database.values()) {
			if (d.name.equalsIgnoreCase(name)) 
				return d;
		}
        return null;
	}

	@Override
	public ProductData findById(int id) {
		return database.get(id);
	}

	@Override
	public List<ProductData> findAll() {
		return new ArrayList<>(database.values());
	}

	@Override
	public ProductData save(ProductData productData) {
		sequence++;
        ProductData savedData = new ProductData();
        savedData.id = sequence;
        savedData.name = productData.name;
        savedData.description = productData.description;
        savedData.price = productData.price;
        savedData.stockQuantity = productData.stockQuantity;
        savedData.imageUrl = productData.imageUrl;
        savedData.categoryId = productData.categoryId;
        
        // Sao chép tất cả thuộc tính (kể cả null)
        savedData.cpu = productData.cpu;
        savedData.ram = productData.ram;
        savedData.screenSize = productData.screenSize;
        savedData.connectionType = productData.connectionType;
        savedData.dpi = productData.dpi;
        savedData.switchType = productData.switchType;
        savedData.layout = productData.layout;
        
        database.put(sequence, savedData);
        
        return savedData;
	}

	@Override
	public ProductData update(ProductData productData) {
		if (database.containsKey(productData.id)) {
            // (Giả lập logic "update" bằng cách sao chép)
            ProductData updatedData = new ProductData();
            updatedData.id = sequence;
            updatedData.name = productData.name;
            updatedData.description = productData.description;
            updatedData.price = productData.price;
            updatedData.stockQuantity = productData.stockQuantity;
            updatedData.imageUrl = productData.imageUrl;
            updatedData.categoryId = productData.categoryId;
            
            // Sao chép tất cả thuộc tính (kể cả null)
            updatedData.cpu = productData.cpu;
            updatedData.ram = productData.ram;
            updatedData.screenSize = productData.screenSize;
            updatedData.connectionType = productData.connectionType;
            updatedData.dpi = productData.dpi;
            updatedData.switchType = productData.switchType;
            updatedData.layout = productData.layout;
            database.put(updatedData.id, updatedData);
            return updatedData;
        }
        return null;
	}

	@Override
	public void deleteById(int id) {
		database.remove(id);
		
	}

	@Override
	public int countByCategoryId(int categoryId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<ProductData> searchByName(String keyword) {
		List<ProductData> results = new ArrayList<>();
		if (keyword == null || keyword.trim().isEmpty()) {
	        return results; // Không tìm được gì thì trả về danh sách rỗng
	    }

	    String lowerKeyword = keyword.toLowerCase(Locale.ROOT);

	    for (ProductData data : database.values()) {
	        if (data.name != null 
	            && data.name.toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
	            results.add(data);
	        }
	    }

	    return results; 
	}

}
