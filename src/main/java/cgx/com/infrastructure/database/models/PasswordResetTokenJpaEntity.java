package cgx.com.infrastructure.database.models;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenJpaEntity {

    @Id
    @Column(name = "token_id", length = 36)
    private String tokenId;

    @Column(name = "hashed_token", nullable = false)
    private String hashedToken;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public PasswordResetTokenJpaEntity() {}

    // Getters & Setters
    public String getTokenId() { return tokenId; }
    public void setTokenId(String tokenId) { this.tokenId = tokenId; }

    public String getHashedToken() { return hashedToken; }
    public void setHashedToken(String hashedToken) { this.hashedToken = hashedToken; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}