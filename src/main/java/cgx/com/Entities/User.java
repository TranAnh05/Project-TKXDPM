package cgx.com.Entities;

import java.time.Instant;
import java.util.regex.Pattern;

public class User {
	// Biểu thức Regular Expression (Regex) đơn giản để kiểm tra email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    
    // (Thêm pattern cho SĐT, ví dụ SĐT Việt Nam 10 số)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0[3|5|7|8|9])([0-9]{8})$");

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
    
    /**
     * Kiểm tra quy tắc của Số Điện Thoại (MỚI).
     * @param phoneNumber SĐT cần kiểm tra
     * @throws IllegalArgumentException Nếu SĐT không rỗng và không đúng định dạng.
     */
    public static void validatePhoneNumber(String phoneNumber) throws IllegalArgumentException {
        // Quy tắc nghiệp vụ: SĐT có thể để trống (null hoặc rỗng)
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return; // Hợp lệ
        }
        // Nếu không rỗng, nó phải đúng định dạng
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng (yêu cầu 10 số, bắt đầu bằng 03, 05, 07, 08, 09).");
        }
    }
    
    /**
     * Kiểm tra quy tắc của Role (Vai trò) (MỚI).
     * @param role Chuỗi vai trò (ví dụ: "CUSTOMER")
     * @return UserRole Enum
     * @throws IllegalArgumentException Nếu chuỗi vai trò không hợp lệ.
     */
    public static UserRole validateRole(String role) throws IllegalArgumentException {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Vai trò (Role) không được để trống.");
        }
        try {
            // Thử chuyển đổi String sang Enum. Nếu thất bại, nó sẽ ném ra
            // một IllegalArgumentException.
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Vai trò (Role) không hợp lệ: " + role);
        }
    }

    /**
     * Kiểm tra quy tắc của Account Status (Trạng thái) (MỚI).
     * @param status Chuỗi trạng thái (ví dụ: "ACTIVE")
     * @return AccountStatus Enum
     * @throws IllegalArgumentException Nếu chuỗi trạng thái không hợp lệ.
     */
    public static AccountStatus validateStatus(String status) throws IllegalArgumentException {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái (Status) không được để trống.");
        }
        try {
            return AccountStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái (Status) không hợp lệ: " + status);
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
    
    /**
     * Thay đổi mật khẩu (MỚI).
     * Được gọi sau khi Use Case đã validate (cả pass cũ và mới)
     * và đã băm mật khẩu mới.
     * @param newHashedPassword Mật khẩu MỚI đã được băm
     */
    public void changePassword(String newHashedPassword) {
        this.hashedPassword = newHashedPassword;
        this.touch(); // Cập nhật thời gian
    }
    
    /**
     * Cập nhật thông tin hồ sơ cơ bản (MỚI).
     * Được gọi sau khi dữ liệu đã được Use Case validate.
     */
    public void updateProfile(String firstName, String lastName, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = (phoneNumber == null || phoneNumber.trim().isEmpty()) ? null : phoneNumber.trim();
        this.touch(); // Cập nhật thời gian
    }
    
    private void touch() {
        this.updatedAt = Instant.now();
    }
    
    /**
     * Thực hiện "Xóa mềm" (Soft Delete) tài khoản này (MỚI).
     * Được gọi bởi Use Case của Admin.
     * Phương thức này ẩn danh thông tin cá nhân (PII)
     * và vô hiệu hóa tài khoản.
     */
    public void softDelete() {
        // 1. Ẩn danh Email
        this.email = anonymizeEmail(this.email, this.userId);
        
        // 2. Xóa thông tin cá nhân (PII)
        this.firstName = "Deleted";
        this.lastName = "User";
        this.phoneNumber = null;
        
        // 3. Vô hiệu hóa mật khẩu (để không ai có thể đăng nhập, kể cả Admin)
        this.hashedPassword = "DELETED_" + Instant.now().toString();
        
        // 4. Đặt trạng thái
        this.status = AccountStatus.DELETED;
        
        this.touch(); // Cập nhật thời gian
    }
    
    /**
     * Hàm helper để tạo một email ẩn danh duy nhất.
     * Ví dụ: "john.doe@gmail.com" -> "deleted_user123_john.doe@gmail.com"
     */
    private String anonymizeEmail(String oldEmail, String userId) {
        String uniquePrefix = "deleted_" + userId + "_";
        // Lấy phần @domain
        String domainPart = "";
        int atIndex = oldEmail.lastIndexOf('@');
        if (atIndex != -1) {
            domainPart = oldEmail.substring(atIndex); // ví dụ: "@gmail.com"
        }
        return uniquePrefix + oldEmail.substring(0, Math.min(oldEmail.length(), 50)) + domainPart;
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
