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

    // --- CÁC PHƯƠNG THỨC VALIDATION TĨNH 
    public static void validateId(String id) {
    	 if (id == null || id.trim().isEmpty()) {
             throw new IllegalArgumentException("ID người dùng không được để trống");
         }
    }
    
    public static void validateEmail(String email) throws IllegalArgumentException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }
    }

    public static void validatePassword(String password) throws IllegalArgumentException {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        // Quy tắc nghiệp vụ: Mật khẩu phải có ít nhất 8 ký tự
        if (password.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
    }
    
    public static void validateName(String firstName, String lastName) throws IllegalArgumentException {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên không được để trống.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ không được để trống.");
        }
    }
    
    
    public static void validatePhoneNumber(String phoneNumber) throws IllegalArgumentException {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được rỗng.");
        }
        
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng (yêu cầu 10 số, bắt đầu bằng 03, 05, 07, 08, 09).");
        }
    }
    
    public static UserRole validateRole(String role) throws IllegalArgumentException {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Vai trò (Role) không hợp lệ: " + role);
        }
    }

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
    
    public static void validateIsAdmin(UserRole role) {
    	 if (role != UserRole.ADMIN) {
             throw new SecurityException("Không có quyền truy cập.");
         }
    }
    

    // --- PHƯƠNG THỨC FACTORY TĨNH ---
    public static User createNewCustomer(String userId, String email, String hashedPassword, String firstName, String lastName) {
        Instant now = Instant.now();
        return new User(
            userId, email, hashedPassword, firstName, lastName,
            null, UserRole.CUSTOMER, AccountStatus.PENDING_VERIFICATION, now, now
        );
    }
    
    // --- CÁC PHƯƠNG THỨC INSTANCE 
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
    
    public void activate() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }
    
    public void validateLoginStatus() {
    	if (this.status != AccountStatus.ACTIVE) {
            throw new SecurityException("Tài khoản không được phép đăng nhập.");
        }
    }
    public void changePassword(String newHashedPassword) {
        this.hashedPassword = newHashedPassword;
        this.touch(); // Cập nhật thời gian
    }
    
    public void updateProfile(String firstName, String lastName, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = (phoneNumber == null || phoneNumber.trim().isEmpty()) ? null : phoneNumber.trim();
        this.touch(); // Cập nhật thời gian
    }
    
    public void touch() {
        this.updatedAt = Instant.now();
    }
    
    public void validateAdminSelfUpdate(String userId) {
    	 if (userId.equals(this.userId)) {
             throw new SecurityException("Admin không thể tự cập nhật vai trò/trạng thái của chính mình. " +
                                         "Vui lòng sử dụng chức năng 'Cập nhật Hồ sơ' thông thường.");
         }
    }
    
    public void softDelete() {
        this.status = AccountStatus.DELETED;
        this.touch();
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
    
    public void setHashedPassword(String hashedPassword) {
    	this.hashedPassword = hashedPassword;
    }

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
    
    
}
