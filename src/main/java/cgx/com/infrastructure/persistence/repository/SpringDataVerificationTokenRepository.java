package cgx.com.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cgx.com.infrastructure.persistence.entity.VerificationTokenJpaEntity;

public interface SpringDataVerificationTokenRepository extends JpaRepository<VerificationTokenJpaEntity, String>{
	// JpaRepository đã có sẵn findById (tương ứng findByToken vì token là @Id) và deleteById
}
