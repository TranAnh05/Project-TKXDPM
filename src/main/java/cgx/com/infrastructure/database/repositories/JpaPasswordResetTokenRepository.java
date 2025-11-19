package cgx.com.infrastructure.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cgx.com.infrastructure.database.models.PasswordResetTokenJpaEntity;

@Repository
public interface JpaPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenJpaEntity, String> {
    // Tìm theo hashed token
    PasswordResetTokenJpaEntity findByHashedToken(String hashedToken);
}
