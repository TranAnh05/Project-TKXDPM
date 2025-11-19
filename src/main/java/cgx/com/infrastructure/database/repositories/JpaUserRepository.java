package cgx.com.infrastructure.database.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cgx.com.infrastructure.database.models.UserJpaEntity;

/**
 * MENTOR NOTE:
 * Đây là "phép thuật" của Spring Boot.
 * Bạn KHÔNG CẦN viết class implement interface này. Spring tự làm.
 * * JpaRepository<UserJpaEntity, String> nghĩa là:
 * - Quản lý bảng map với UserJpaEntity.
 * - Khóa chính có kiểu String.
 * * Nó cung cấp sẵn các hàm: save(), findById(), findAll(), deleteById()...
 */
@Repository
public interface JpaUserRepository extends JpaRepository<UserJpaEntity, String>{
	// Bạn muốn tìm theo email? Chỉ cần đặt tên hàm đúng quy tắc:
    // findBy + TênThuộcTính
    UserJpaEntity findByEmail(String email);
    
    // Tìm theo SĐT?
    UserJpaEntity findByPhoneNumber(String phoneNumber);
    
 // --- HÀM MỚI: Tìm kiếm theo từ khóa (Email hoặc Tên) ---
    @Query("SELECT u FROM UserJpaEntity u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<UserJpaEntity> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    // --- HÀM MỚI: Đếm số lượng kết quả tìm kiếm ---
    @Query("SELECT COUNT(u) FROM UserJpaEntity u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    long countUsers(@Param("keyword") String keyword);
}
