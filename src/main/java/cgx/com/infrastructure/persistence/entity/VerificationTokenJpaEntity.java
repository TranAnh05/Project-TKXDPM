package cgx.com.infrastructure.persistence.entity;

import jakarta.persistence.Entity;

import java.time.Instant;

import cgx.com.usecase.Interface_Common.VerificationTokenData;
import jakarta.persistence.*;

@Entity
@Table(name = "verification_tokens")
public class VerificationTokenJpaEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Instant expiryDate;

    public VerificationTokenJpaEntity() {}

    public VerificationTokenJpaEntity(String token, String userId, Instant expiryDate) {
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
    }
    
    // Mappers
    public VerificationTokenData toData() {
        return new VerificationTokenData(this.token, this.userId, this.expiryDate);
    }

    public static VerificationTokenJpaEntity fromData(VerificationTokenData data) {
        return new VerificationTokenJpaEntity(data.token, data.userId, data.expiryDate);
    }
}