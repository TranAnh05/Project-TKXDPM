package cgx.com.usecase.ManageUser.VerifyPasswordReset;

/**
 * DTO cho dữ liệu đầu ra (Xác thực Đặt lại Mật khẩu).
 * Chỉ cần thông báo thành công/thất bại.
 */
public class VerifyPasswordResetResponseData {
    public boolean success;
    public String message;
}