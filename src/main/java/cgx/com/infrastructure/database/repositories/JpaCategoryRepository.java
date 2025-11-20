package cgx.com.infrastructure.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cgx.com.infrastructure.database.models.CategoryJpaEntity;

@Repository
public interface JpaCategoryRepository extends JpaRepository<CategoryJpaEntity, String> {
    CategoryJpaEntity findByName(String name);
    
    // Kiểm tra xem có danh mục nào có parentId là id này không (để check hasChildren)
    boolean existsByParentCategoryId(String parentId);
}