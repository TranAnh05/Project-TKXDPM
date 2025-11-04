package application.ports.out.ManageCategory;

import java.util.List;

import application.dtos.ManageCategory.CategoryData;

public interface CategoryRepository {
	CategoryData findByName(String name);
    CategoryData save(CategoryData categoryData);
    CategoryData findById(int id);
    List<CategoryData> findAll();
    CategoryData update(CategoryData categoryData);
    void deleteById(int id);
    int countProductsByCategoryId(int categoryId);
}
