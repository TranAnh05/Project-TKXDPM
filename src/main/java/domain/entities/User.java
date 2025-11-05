package domain.entities;

public class User {
	private int id;
    private String email;
    private String passwordHash; // QUAN TRỌNG: Chỉ lưu hash
    private String fullName;
    private String address;
    private Role role;
    private boolean isBlocked;
    
    /**
     * Constructor "sạch" (Anemic):
     * Được gọi bởi UseCase (Tầng 3) SAU KHI đã validate.
     */
    public User(String email, String passwordHash, String fullName, String address, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.address = address;
        this.role = role;
        this.isBlocked = false; // Mặc định
    }
    
    /**
     * Constructor để tải từ CSDL
     */
    public User(int id, String email, String passwordHash, String fullName, String address, Role role, boolean isBlocked) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.address = address;
        this.role = role;
        this.isBlocked = isBlocked;
    }
    
    public static void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Định dạng email không hợp lệ.");
        }
    }
    
    public static void validatePassword(String password) {
        if (password == null || password.trim().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
    }
    
    public static void validateFullName(String name) {
    	 if (name == null || name.trim().isEmpty()) {
             throw new IllegalArgumentException("Tên không được để trống.");
         }
    }
    
    public static void validateAddress(String address) {
   	 if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được để trống.");
        }
   }
    
    public static void validateRole(String roleName) {
    	if(roleName == null || roleName.trim().isEmpty()) {
    		throw new IllegalArgumentException("Vai trò không được rỗng.");
    	}
    	else {
    		boolean hasRole = false;
    		for(Role role : Role.values()) {
    			if(roleName.equalsIgnoreCase(role.name()))
    				hasRole = true;
    		}
    		
    		if(!hasRole) {
    			throw new IllegalArgumentException("Vai trò không hợp lệ.");
    		}
    	}
    }

    // --- Logic nghiệp vụ (cho UseCase sau) ---
    public void setRole(Role newRole) {
        this.role = newRole;
    }
    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getAddress() { return address; }
    public Role getRole() { return role; }
    public boolean isBlocked() { return isBlocked; }
}
