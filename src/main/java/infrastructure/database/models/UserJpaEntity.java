package infrastructure.database.models;

import jakarta.persistence.*; // Thư viện JPA
import java.time.Instant;

/**
 * MENTOR NOTE:
 * Đây là một JPA Entity. Nó đại diện cho một HÀNG (row) trong bảng 'users' của MySQL.
 * * - @Entity: Báo cho Spring biết class này map với Database.
 * - @Table: Tên bảng trong MySQL.
 * - @Id: Khóa chính (Primary Key).
 * - @Column: Cấu hình cột (ví dụ: unique, nullable).
 */
@Entity
@Table(name = "users") // Tên bảng trong MySQL
public class UserJpaEntity {

    @Id
    @Column(name = "user_id", length = 36) // VARCHAR(36) cho UUID
    private String userId;

    @Column(name = "email", unique = true, nullable = false) // Email không được trùng, không được rỗng
    private String email;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "role")
    private String role; // Lưu Enum dưới dạng String ("ADMIN", "CUSTOMER")

    @Column(name = "status")
    private String status; // Lưu Enum dưới dạng String

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // --- Constructor rỗng (Bắt buộc cho JPA) ---
    public UserJpaEntity() {}

    // --- Getters & Setters (Spring cần để đọc/ghi dữ liệu) ---
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
