package infrastructure.adapters;

import java.util.List;

import org.springframework.stereotype.Component;

import Entities.AccountStatus;
import Entities.UserRole;
import infrastructure.database.models.UserJpaEntity;
import infrastructure.database.repositories.JpaUserRepository;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;
import usecase.ManageUser.SearchUsers.UserSearchCriteria;

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
        // Tạm thời trả về rỗng để code biên dịch được
        return List.of(); 
    }

    @Override
    public long count(UserSearchCriteria criteria) {
        return jpaRepository.count();
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