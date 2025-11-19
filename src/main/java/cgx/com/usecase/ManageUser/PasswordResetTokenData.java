package cgx.com.usecase.ManageUser;

import java.time.Instant;

/**
 * DTO dùng để giao tiếp với IPasswordResetTokenRepository.
 * Đây là "bản sao" dữ liệu của Entity PasswordResetToken.
 */
public class PasswordResetTokenData {
    public String tokenId;
    public String hashedToken;
    public String userId;
    public Instant expiresAt;

    public PasswordResetTokenData(String tokenId, String hashedToken, String userId, Instant expiresAt) {
        this.tokenId = tokenId;
        this.hashedToken = hashedToken;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }
}
