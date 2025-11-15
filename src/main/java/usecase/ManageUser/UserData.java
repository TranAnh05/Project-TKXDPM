package usecase.ManageUser;

import java.time.Instant;

import Entities.AccountStatus;
import Entities.UserRole;

/**
 * DTO dùng để giao tiếp với IUserRepository.
 * Đây là "bản sao" dữ liệu của Entity User, dùng để
 * vận chuyển qua các tầng mà không làm lộ Entity.
 */
public class UserData {
    // Các trường này là public để dễ dàng mapping
    public String userId;
    public String email;
    public String hashedPassword;
    public String firstName;
    public String lastName;
    public String phoneNumber;
    public UserRole role;
    public AccountStatus status;
    public Instant createdAt;
    public Instant updatedAt;

    // Constructor rỗng (nếu cần)
    public UserData() {}

    // Constructor đầy đủ
    public UserData(String userId, String email, String hashedPassword, String firstName, String lastName,
                    String phoneNumber, UserRole role, AccountStatus status, Instant createdAt, Instant updatedAt) {
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
}