package cgx.com.usecase.ManageUser;

import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

/**
 * Interface cho dịch vụ Xác thực Token (ví dụ: JWT).
 * Đây là "mặt đối lập" của IAuthTokenGenerator.
 * Nó nhận một chuỗi token và "biên dịch" nó thành
 * thông tin người dùng (AuthPrincipal).
 */
public interface IAuthTokenValidator {
    /**
     * Xác thực và giải mã một chuỗi token.
     * @param token Chuỗi token (ví dụ: "Bearer eyJ...")
     * @return AuthPrincipal chứa thông tin (userId, email, role)
     * @throws SecurityException Nếu token không hợp lệ, hết hạn, hoặc bị lỗi.
     */
    AuthPrincipal validate(String token) throws SecurityException;
}