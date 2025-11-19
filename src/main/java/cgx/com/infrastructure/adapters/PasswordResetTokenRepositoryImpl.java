package cgx.com.infrastructure.adapters;

import org.springframework.stereotype.Component;

import cgx.com.infrastructure.database.models.PasswordResetTokenJpaEntity;
import cgx.com.infrastructure.database.repositories.JpaPasswordResetTokenRepository;
import cgx.com.usecase.ManageUser.IPasswordResetTokenRepository;
import cgx.com.usecase.ManageUser.PasswordResetTokenData;

@Component
public class PasswordResetTokenRepositoryImpl implements IPasswordResetTokenRepository {

    private final JpaPasswordResetTokenRepository jpaRepository;

    public PasswordResetTokenRepositoryImpl(JpaPasswordResetTokenRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(PasswordResetTokenData tokenData) {
        PasswordResetTokenJpaEntity entity = new PasswordResetTokenJpaEntity();
        entity.setTokenId(tokenData.tokenId);
        entity.setHashedToken(tokenData.hashedToken);
        entity.setUserId(tokenData.userId);
        entity.setExpiresAt(tokenData.expiresAt);
        jpaRepository.save(entity);
    }

    @Override
    public PasswordResetTokenData findByHashedToken(String hashedToken) {
        PasswordResetTokenJpaEntity entity = jpaRepository.findByHashedToken(hashedToken);
        if (entity == null) return null;
        return new PasswordResetTokenData(
            entity.getTokenId(), entity.getHashedToken(), entity.getUserId(), entity.getExpiresAt()
        );
    }

    @Override
    public void deleteByTokenId(String tokenId) {
        jpaRepository.deleteById(tokenId);
    }
}
