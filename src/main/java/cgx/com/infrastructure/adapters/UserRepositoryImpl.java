package cgx.com.infrastructure.adapters;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;
import cgx.com.infrastructure.database.models.UserJpaEntity;
import cgx.com.infrastructure.database.repositories.JpaUserRepository;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.SearchUsers.UserSearchCriteria;

/**
 * MENTOR NOTE:
 * Đây là lớp Adapter quan trọng nhất.
 * Nhiệm vụ:
 * 1. Nhận lệnh từ Use Case (qua interface IUserRepository).
 * 2. Gọi Spring Data JPA (JpaUserRepository) để lấy dữ liệu từ DB.
 * 3. Chuyển đổi (Map) dữ liệu từ JPA Entity -> UserData DTO.
 * 4. Trả về cho Use Case.
 * * Use Case KHÔNG BAO GIỜ biết về JPA hay MySQL. Nó chỉ biết UserData.
 */
@Component // Spring sẽ quản lý class này
public class UserRepositoryImpl implements IUserRepository {

    private final JpaUserRepository jpaRepository;

    // Dependency Injection: Spring tự động đưa JpaUserRepository vào đây
    public UserRepositoryImpl(JpaUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserData findByEmail(String email) {
        UserJpaEntity entity = jpaRepository.findByEmail(email);
        if (entity == null) return null;
        return mapToUserData(entity);
    }

    @Override
    public UserData findByUserId(String userId) {
        // jpaRepository.findById trả về Optional, ta dùng orElse(null)
        return jpaRepository.findById(userId)
                .map(this::mapToUserData)
                .orElse(null);
    }

    @Override
    public UserData save(UserData userData) {
        UserJpaEntity entity = mapToJpaEntity(userData);
        UserJpaEntity savedEntity = jpaRepository.save(entity);
        return mapToUserData(savedEntity);
    }

    @Override
    public UserData update(UserData userData) {
        // save() trong JPA hoạt động như "Upsert" (có rồi thì update, chưa có thì insert)
        return save(userData);
    }

    // --- MENTOR NOTE: Phần Search/Count chúng ta sẽ làm sau (cần kiến thức Query nâng cao) ---
    @Override
    public List<UserData> search(UserSearchCriteria criteria, int pageNumber, int pageSize) {
    	// 1. Tạo đối tượng phân trang (Trang số mấy, lấy bao nhiêu dòng)
        // Sort.by("createdAt").descending(): Sắp xếp người mới tạo lên đầu
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());

        // 2. Lấy từ khóa tìm kiếm, nếu null thì truyền chuỗi rỗng để SQL không lỗi
        String keyword = (criteria.getSearchTerm() == null) ? "" : criteria.getSearchTerm();

        // 3. Gọi hàm @Query searchUsers bạn đã viết
        Page<UserJpaEntity> pageResult = jpaRepository.searchUsers(keyword, pageable);

        // 4. Chuyển đổi (Map) từ List<UserJpaEntity> sang List<UserData> để trả về cho UseCase
        return pageResult.getContent().stream()
                .map(this::mapToUserData)
                .collect(Collectors.toList());
    }

    @Override
    public long count(UserSearchCriteria criteria) {
    	// 1. Lấy từ khóa
        String keyword = (criteria.getSearchTerm() == null) ? "" : criteria.getSearchTerm();
        
        // 2. Gọi hàm @Query countUsers
        return jpaRepository.countUsers(keyword);
    }

    // --- Helper Mapping Methods (Chuyển đổi dữ liệu) ---

    private UserData mapToUserData(UserJpaEntity entity) {
        return new UserData(
            entity.getUserId(),
            entity.getEmail(),
            entity.getHashedPassword(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getPhoneNumber(),
            UserRole.valueOf(entity.getRole()), // String -> Enum
            AccountStatus.valueOf(entity.getStatus()), // String -> Enum
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private UserJpaEntity mapToJpaEntity(UserData data) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUserId(data.userId);
        entity.setEmail(data.email);
        entity.setHashedPassword(data.hashedPassword);
        entity.setFirstName(data.firstName);
        entity.setLastName(data.lastName);
        entity.setPhoneNumber(data.phoneNumber);
        entity.setRole(data.role.name()); // Enum -> String
        entity.setStatus(data.status.name()); // Enum -> String
        entity.setCreatedAt(data.createdAt);
        entity.setUpdatedAt(data.updatedAt);
        return entity;
    }
}