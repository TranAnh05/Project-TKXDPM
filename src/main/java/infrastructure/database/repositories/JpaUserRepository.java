package infrastructure.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import infrastructure.database.models.UserJpaEntity;

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
}
