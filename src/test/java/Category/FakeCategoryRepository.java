package Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import usecase.ManageCategory.CategoryData;
import usecase.ManageCategory.CategoryRepository;

public class FakeCategoryRepository implements CategoryRepository{
	private Map<Integer, CategoryData> database = new HashMap<>();
	private int sequence = 0;
	
	// CSDL Giả cho bảng 'product' (chỉ cần ID)
    // Map<productId, categoryId>
    private Map<Integer, Integer> productToCategoryMap = new HashMap<>();

	@Override
	public CategoryData findByName(String name) {
		for (CategoryData d : database.values()) { if (d.name.equalsIgnoreCase(name)) return d; }
        return null;
	}

	@Override
	public CategoryData save(CategoryData d) {
		sequence++;
        CategoryData saved = new CategoryData(sequence, d.name);
        database.put(sequence, saved);
        return saved;
	}

	@Override
	public CategoryData findById(int id) {
		return database.get(id);
	}

	@Override
	public List<CategoryData> findAll() {
		return new ArrayList<>(database.values());
	}

	@Override
	public CategoryData update(CategoryData categoryData) {
		if (database.containsKey(categoryData.id)) {
            // Tạo một bản sao mới (giống CSDL)
            CategoryData updatedData = new CategoryData(
                categoryData.id, 
                categoryData.name
            );
            database.put(categoryData.id, updatedData); // Ghi đè
            return updatedData;
        }
        return null; // Không tìm thấy
	}

	@Override
	public void deleteById(int id) {
		database.remove(id);
	}

	@Override
	public int countProductsByCategoryId(int categoryId) {
		int count = 0;
        for (Integer catId : productToCategoryMap.values()) {
            if (catId == categoryId) {
                count++;
            }
        }
        return count;
	}
	
	// === Hàm helper cho Test ===
    /**
     * Giả lập việc thêm một sản phẩm vào CSDL
     */
	public void addFakeProduct(int productId, int categoryId) {
        productToCategoryMap.put(productId, categoryId);
    }
}
