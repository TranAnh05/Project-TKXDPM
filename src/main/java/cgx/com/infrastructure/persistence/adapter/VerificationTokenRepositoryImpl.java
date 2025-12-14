package cgx.com.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;

import cgx.com.infrastructure.persistence.entity.VerificationTokenJpaEntity;
import cgx.com.infrastructure.persistence.repository.SpringDataVerificationTokenRepository;
import cgx.com.usecase.Interface_Common.IVerificationTokenRepository;
import cgx.com.usecase.Interface_Common.VerificationTokenData;

@Component
public class VerificationTokenRepositoryImpl implements IVerificationTokenRepository {

    private final SpringDataVerificationTokenRepository jpaRepository;

    public VerificationTokenRepositoryImpl(SpringDataVerificationTokenRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(VerificationTokenData tokenData) {
        jpaRepository.save(VerificationTokenJpaEntity.fromData(tokenData));
    }

    @Override
    public VerificationTokenData findByToken(String token) {
        return jpaRepository.findById(token)
                .map(VerificationTokenJpaEntity::toData)
                .orElse(null);
    }

    @Override
    public void deleteByToken(String token) {
        jpaRepository.deleteById(token);
    }
}
