package cgx.com.usecase.ManageUser;

/**
 * Interface cho dịch vụ gửi Email (cổng ra bên ngoài).
 */
public interface IEmailService {
    /**
     * Gửi email đặt lại mật khẩu.
     * @param toEmail Email của người nhận
     * @param userName Tên của người nhận (ví dụ: "John Doe")
     * @param plainTextToken Token (dạng plain-text) để nhúng vào link
     */
    void sendPasswordResetEmail(String toEmail, String userName, String plainTextToken);
    
    /**
     * Gửi email xác thực tài khoản sau khi đăng ký.
     * @param toEmail Email người nhận
     * @param userName Tên người dùng
     * @param verificationToken Mã/Link xác thực
     */
    void sendVerificationEmail(String toEmail, String userName, String verificationToken);
    
    /**
     * Gửi email cảnh báo khi mật khẩu thay đổi thành công.
     * @param toEmail Email người nhận
     * @param userName Tên người dùng
     */
    void sendPasswordChangeAlert(String toEmail, String userName);
    
    /**
     * Gửi email chào mừng khi Admin tạo tài khoản thủ công.
     * Thường chứa thông tin đăng nhập ban đầu.
     * @param toEmail Email người nhận
     * @param userName Tên người nhận
     * @param initialPassword Mật khẩu khởi tạo (để user đăng nhập lần đầu)
     */
    void sendAccountCreatedEmail(String toEmail, String userName, String initialPassword);
}