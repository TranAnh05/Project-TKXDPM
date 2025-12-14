package cgx.com.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cgx.com.infrastructure.persistence.entity.UserJpaEntity;


@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, String> {
    Optional<UserJpaEntity> findByEmail(String email);

    // Hỗ trợ tìm kiếm cho use case SearchUsers
    // Tìm theo email HOẶC tên có chứa từ khóa (không phân biệt hoa thường)
    @Query("SELECT u FROM UserJpaEntity u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UserJpaEntity> searchByKeyword(String keyword, Pageable pageable);
}
