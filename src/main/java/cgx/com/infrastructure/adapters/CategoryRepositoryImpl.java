package cgx.com.infrastructure.adapters;

import org.springframework.stereotype.Component;

import cgx.com.infrastructure.database.models.CategoryJpaEntity;
import cgx.com.infrastructure.database.repositories.JpaCategoryRepository;
import cgx.com.usecase.ManageCategory.CategoryData;
import cgx.com.usecase.ManageCategory.ICategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryRepositoryImpl implements ICategoryRepository {

    private final JpaCategoryRepository jpaRepository;

    public CategoryRepositoryImpl(JpaCategoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(CategoryData categoryData) {
        CategoryJpaEntity entity = mapToEntity(categoryData);
        jpaRepository.save(entity);
    }

    @Override
    public CategoryData findByName(String name) {
        CategoryJpaEntity entity = jpaRepository.findByName(name);
        if (entity == null) return null;
        return mapToData(entity);
    }

    @Override
    public CategoryData findById(String id) {
        return jpaRepository.findById(id)
                .map(this::mapToData)
                .orElse(null);
    }

    @Override
    public boolean hasChildren(String parentId) {
        return jpaRepository.existsByParentCategoryId(parentId);
    }

    @Override
    public boolean hasProducts(String categoryId) {
        // TODO: Sau này khi có Product Repository, chúng ta sẽ check thật sự.
        // Hiện tại trả về false để code chạy được.
        return false; 
    }

    @Override
    public void delete(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<CategoryData> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::mapToData)
                .collect(Collectors.toList());
    }

    // --- Mappers ---
    private CategoryData mapToData(CategoryJpaEntity entity) {
        return new CategoryData(
            entity.getCategoryId(),
            entity.getName(),
            entity.getDescription(),
            entity.getParentCategoryId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private CategoryJpaEntity mapToEntity(CategoryData data) {
        CategoryJpaEntity entity = new CategoryJpaEntity();
        entity.setCategoryId(data.categoryId);
        entity.setName(data.name);
        entity.setDescription(data.description);
        entity.setParentCategoryId(data.parentCategoryId);
        entity.setCreatedAt(data.createdAt);
        entity.setUpdatedAt(data.updatedAt);
        return entity;
    }
}