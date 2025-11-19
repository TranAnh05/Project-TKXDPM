package cgx.com.infrastructure.adapters;

import java.util.Base64;

import org.springframework.stereotype.Component;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageUser.IAuthTokenGenerator;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

/**
 * Triển khai đơn giản cho Token Service.
 * MENTOR NOTE: Trong môi trường Production thực tế, bạn nên dùng thư viện 'jjwt'
 * và có private key bí mật.
 * Ở đây, để đơn giản cho việc học luồng Clean Arch, tôi sẽ giả lập một token
 * dạng chuỗi base64 đơn giản (MÔ PHỎNG JWT).
 */
@Component
public class JwtTokenService implements IAuthTokenGenerator, IAuthTokenValidator {

    @Override
    public String generate(String userId, String email, UserRole role) {
        // MÔ PHỎNG: Tạo một chuỗi fake-token chứa thông tin
        // Format: "fake-jwt-header." + Base64(userId:email:role)
        String payload = userId + ":" + email + ":" + role.name();
        String encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes());
        return "fake-header." + encodedPayload + ".fake-signature";
    }

    @Override
    public AuthPrincipal validate(String token) throws SecurityException {
        try {
            // Loại bỏ "Bearer " nếu có
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // Giải mã fake-token
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new SecurityException("Token không đúng định dạng");
            
            String payload = new String(Base64.getDecoder().decode(parts[1]));
            String[] data = payload.split(":");
            
            String userId = data[0];
            String email = data[1];
            UserRole role = UserRole.valueOf(data[2]);
            
            return new AuthPrincipal(userId, email, role);
        } catch (Exception e) {
            throw new SecurityException("Token không hợp lệ: " + e.getMessage());
        }
    }
}