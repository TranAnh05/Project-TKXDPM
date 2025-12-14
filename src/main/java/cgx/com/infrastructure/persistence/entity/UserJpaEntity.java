package cgx.com.infrastructure.persistence.entity;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageUser.UserData;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users")
@Data // Lombok sinh Getter/Setter tự động
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity {

    @Id
    @Column(name = "user_id")
    private String userId; // Dùng String (UUID) làm khóa chính

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String hashedPassword;

    private String firstName;
    private String lastName;
    private String phoneNumber;

    @Enumerated(EnumType.STRING) // Lưu Enum dưới dạng chuỗi ("ADMIN", "CUSTOMER")
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    // --- MAPPERS (Quan trọng: Chuyển đổi qua lại giữa Domain và JPA) ---

    public UserJpaEntity(String userId, String email, String hashedPassword, String firstName, String lastName, String phoneNumber, UserRole role, AccountStatus status, Instant createdAt, Instant updatedAt) {
        this.userId = userId;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public UserJpaEntity() {}

	// 1. Từ JPA -> Domain (Để trả về cho Use Case)
    public User toDomain() {
        return new User(
            this.userId,
            this.email,
            this.hashedPassword,
            this.firstName,
            this.lastName,
            this.phoneNumber,
            this.role,
            this.status,
            this.createdAt,
            this.updatedAt
        );
    }

    // 2. Từ Domain -> JPA (Để lưu vào DB)
    public static UserJpaEntity fromDomain(UserData data) { // Hoặc nhận User domain
        return new UserJpaEntity(
            data.userId,
            data.email,
            data.hashedPassword,
            data.firstName,
            data.lastName,
            data.phoneNumber,
            data.role,
            data.status,
            data.createdAt,
            data.updatedAt
        );
    }

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
    
    
}