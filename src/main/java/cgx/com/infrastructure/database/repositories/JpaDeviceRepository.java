package cgx.com.infrastructure.database.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cgx.com.infrastructure.database.models.DeviceJpaEntity;

import java.math.BigDecimal;

@Repository
public interface JpaDeviceRepository extends JpaRepository<DeviceJpaEntity, String> {
    
    boolean existsByName(String name);

    // Query tìm kiếm phức tạp (Đa tiêu chí)
    @Query("SELECT d FROM DeviceJpaEntity d WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR :categoryId = '' OR d.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR d.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR d.price <= :maxPrice) AND " +
           "(:status IS NULL OR :status = '' OR d.status = :status)")
    Page<DeviceJpaEntity> searchDevices(
            @Param("keyword") String keyword,
            @Param("categoryId") String categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status") String status,
            Pageable pageable
    );

    // Query đếm cho tìm kiếm
    @Query("SELECT COUNT(d) FROM DeviceJpaEntity d WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR :categoryId = '' OR d.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR d.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR d.price <= :maxPrice) AND " +
           "(:status IS NULL OR :status = '' OR d.status = :status)")
    long countDevices(
            @Param("keyword") String keyword,
            @Param("categoryId") String categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status") String status
    );
}
