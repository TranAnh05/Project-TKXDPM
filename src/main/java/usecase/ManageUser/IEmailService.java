package usecase.ManageUser;

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
}