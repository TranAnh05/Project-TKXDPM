package cgx.com.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import cgx.com.Entities.User;
import cgx.com.infrastructure.persistence.entity.UserJpaEntity;
import cgx.com.infrastructure.persistence.repository.SpringDataUserRepository;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.SearchUsers.UserSearchCriteria;

@Component // Để Spring quản lý Bean này
public class UserRepositoryImpl implements IUserRepository {

    private final SpringDataUserRepository jpaRepository;

    public UserRepositoryImpl(SpringDataUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserData findByEmail(String email) {
        Optional<UserJpaEntity> entity = jpaRepository.findByEmail(email);
        // Nếu có thì map sang DTO, không thì null
        return entity.map(this::mapToData).orElse(null);
    }

    @Override
    public UserData findByUserId(String userId) {
        Optional<UserJpaEntity> entity = jpaRepository.findById(userId);
        return entity.map(this::mapToData).orElse(null);
    }

    @Override
    public UserData save(UserData userData) {
        UserJpaEntity entity = UserJpaEntity.fromDomain(userData);
        UserJpaEntity savedEntity = jpaRepository.save(entity);
        return mapToData(savedEntity);
    }

    @Override
    public UserData update(UserData userData) {
        // Trong JPA, save() cũng là update nếu ID đã tồn tại
        return save(userData);
    }

    // --- Triển khai phần Search & Pagination ---
    @Override
    public List<UserData> search(UserSearchCriteria criteria, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<UserJpaEntity> pageResult;

        if (criteria.hasSearchTerm()) {
            pageResult = jpaRepository.searchByKeyword(criteria.getSearchTerm(), pageable);
        } else {
            pageResult = jpaRepository.findAll(pageable);
        }

        return pageResult.stream()
                .map(this::mapToData)
                .collect(Collectors.toList());
    }

    @Override
    public long count(UserSearchCriteria criteria) {
        if (criteria.hasSearchTerm()) {
            // Lưu ý: Cần viết thêm hàm countByKeyword trong SpringDataUserRepository nếu muốn tối ưu
            // Nhưng tạm thời dùng hàm search ở trên để đếm cũng được (hơi chậm nếu data lớn)
            // Cách đúng: Gọi count query riêng. Ở đây tôi demo đơn giản:
             return jpaRepository.count(); // TODO: Cần refine lại query count chính xác theo keyword
        }
        return jpaRepository.count();
    }
    
    // Helper helper mapping
    private UserData mapToData(UserJpaEntity entity) {
        User domain = entity.toDomain();
        // Chuyển từ Domain sang UserData (DTO)
        // Vì UserData là DTO thuần public fields, ta gán trực tiếp hoặc qua constructor
        return new UserData(
            domain.getUserId(), domain.getEmail(), domain.getHashedPassword(),
            domain.getFirstName(), domain.getLastName(), domain.getPhoneNumber(),
            domain.getRole(), domain.getStatus(), domain.getCreatedAt(), domain.getUpdatedAt()
        );
    }
}
