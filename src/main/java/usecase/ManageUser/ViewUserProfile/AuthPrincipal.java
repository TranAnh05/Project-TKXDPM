package usecase.ManageUser.ViewUserProfile;

import Entities.UserRole;

/**
 * DTO (Data Transfer Object) đơn giản
 * Dùng để chứa thông tin đã được giải mã từ một Auth Token.
 * Đây là "danh tính" (Principal) của người dùng đã xác thực.
 */
public class AuthPrincipal {
    public final String userId;
    public final String email;
    public final UserRole role;

    public AuthPrincipal(String userId, String email, UserRole role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }
}