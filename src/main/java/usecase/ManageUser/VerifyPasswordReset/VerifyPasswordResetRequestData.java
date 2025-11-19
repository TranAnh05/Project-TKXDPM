package usecase.ManageUser.VerifyPasswordReset;

/**
 * DTO cho dữ liệu đầu vào (Xác thực Đặt lại Mật khẩu).
 * Chứa token (plain-text) từ URL và mật khẩu mới.
 */
public class VerifyPasswordResetRequestData {
    public final String resetToken; // Token plain-text từ URL
    public final String newPassword;

    public VerifyPasswordResetRequestData(String resetToken, String newPassword) {
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }
}
