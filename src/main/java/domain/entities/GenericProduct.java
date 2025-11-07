package domain.entities;

import java.util.Map;

public class GenericProduct extends Product{

	public GenericProduct(int id, String name, String description, double price, int stockQuantity, String imageUrl,
			int categoryId) {
		super(id, name, description, price, stockQuantity, imageUrl, categoryId);
		// TODO Auto-generated constructor stub
	}
	
	public GenericProduct(String name, String description, double price, int stockQuantity, String imageUrl, int categoryId) {
        super(name, description, price, stockQuantity, imageUrl, categoryId);
    }

	
	@Override
	public void updateSpecific(Map<String, String> attributes) {
		// TODO Auto-generated method stub
		
	}

}
