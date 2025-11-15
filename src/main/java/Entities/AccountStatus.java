package Entities;

public enum AccountStatus {
	/**
     * Tài khoản đang hoạt động bình thường.
     * Đây là trạng thái duy nhất có thể đăng nhập (theo phương thức user.canLogin()).
     */
    ACTIVE,

    /**
     * Tài khoản mới tạo, đang chờ xác thực (ví dụ: xác thực email).
     * (Sử dụng nếu bạn muốn mở rộng chức năng xác thực email).
     */
    PENDING_VERIFICATION,

    /**
     * Tài khoản bị đình chỉ bởi Admin. Không thể đăng nhập.
     */
    SUSPENDED,

    /**
     * Tài khoản đã bị xóa (xóa mềm - soft delete).
     * Không thể đăng nhập và thông tin cá nhân bị ẩn đi.
     */
    DELETED
}
