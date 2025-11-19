package cgx.com.usecase.ManageUser;

import cgx.com.Entities.UserRole;

/**
 * Interface cho dịch vụ tạo Token xác thực (ví dụ: JWT).
 * Use Case (Layer 3) định nghĩa "cổng" (port),
 * Lớp triển khai (Layer 2) sẽ quyết định chi tiết (ví dụ: thư viện `jjwt`).
 */
public interface IAuthTokenGenerator {
    /**
     * Tạo một token mới cho người dùng.
     * @param userId ID của người dùng
     * @param email Email
     * @param role Vai trò (ví dụ: CUSTOMER, ADMIN)
     * @return Một chuỗi token
     */
    String generate(String userId, String email, UserRole role);
}