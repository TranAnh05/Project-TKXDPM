package Entities;

import java.time.Instant;
import java.util.regex.Pattern;

public class User {
	// Biểu thức Regular Expression (Regex) đơn giản để kiểm tra email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private String userId;
    private String email;
    private String hashedPassword;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private AccountStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    // (Constructor, Getters... vẫn như cũ)
    
    // Constructor đầy đủ (dành cho Repository)
    public User(String userId, String email, String hashedPassword, String firstName, String lastName,
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

    // --- CÁC PHƯƠNG THỨC VALIDATION TĨNH (THEO YÊU CẦU) ---

    /**
     * Kiểm tra quy tắc của Email.
     * @param email Email cần kiểm tra
     * @throws IllegalArgumentException Nếu email rỗng hoặc không đúng định dạng.
     */
    public static void validateEmail(String email) throws IllegalArgumentException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }
    }

    /**
     * Kiểm tra quy tắc của Mật khẩu.
     * @param password Mật khẩu (dạng plain-text) cần kiểm tra
     * @throws IllegalArgumentException Nếu mật khẩu quá ngắn.
     */
    public static void validatePassword(String password) throws IllegalArgumentException {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        // Quy tắc nghiệp vụ: Mật khẩu phải có ít nhất 8 ký tự
        if (password.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        // Bạn có thể thêm các quy tắc khác (chữ hoa, chữ thường, số...)
    }

    /**
     * Kiểm tra quy tắc của Tên.
     * @param firstName Tên
     * @param lastName Họ
     * @throws IllegalArgumentException Nếu Tên hoặc Họ bị bỏ trống.
     */
    public static void validateName(String firstName, String lastName) throws IllegalArgumentException {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên không được để trống.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ không được để trống.");
        }
    }

    // --- PHƯƠNG THỨC FACTORY TĨNH ---

    /**
     * Phương thức "Static Factory Method" để tạo một Customer mới.
     * Phương thức này được gọi *SAU KHI* dữ liệu đã được Use Case validate.
     */
    public static User createNewCustomer(String userId, String email, String hashedPassword, String firstName, String lastName) {
        Instant now = Instant.now();
        return new User(
            userId, email, hashedPassword, firstName, lastName,
            null, UserRole.CUSTOMER, AccountStatus.ACTIVE, now, now
        );
    }
    
    // --- CÁC PHƯƠNG THỨC INSTANCE (LOGIC NGHIỆP VỤ CỦA 1 ENTITY) ---

    public boolean canLogin() {
        return this.status == AccountStatus.ACTIVE;
    }
    
    public boolean isAdministrator() {
        return this.role == UserRole.ADMIN;
    }

    public void suspend() {
        this.status = AccountStatus.SUSPENDED;
        this.touch();
    }
    
    private void touch() {
        this.updatedAt = Instant.now();
    }

    // (Getters cho tất cả các trường)
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getHashedPassword() { return hashedPassword; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public UserRole getRole() { return role; }
    public AccountStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
