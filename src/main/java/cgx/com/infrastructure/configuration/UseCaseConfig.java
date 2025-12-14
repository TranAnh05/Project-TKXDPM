package cgx.com.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cgx.com.infrastructure.external.FakeEmailService;
import cgx.com.infrastructure.persistence.adapter.UserRepositoryImpl;
import cgx.com.infrastructure.persistence.adapter.VerificationTokenRepositoryImpl;
import cgx.com.infrastructure.presentation.WebPresenter;
import cgx.com.infrastructure.security.BcryptPasswordHasher;
import cgx.com.infrastructure.security.JwtTokenProvider;
import cgx.com.infrastructure.security.UuidUserIdGenerator;
import cgx.com.usecase.ManageUser.AuthenticateUser.LoginByEmailUseCase;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterByEmailUseCase;
import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailUseCase;

@Configuration
public class UseCaseConfig {

    // 1. Cập nhật Register Use Case (Thêm tokenRepo thật)
    @Bean
    public RegisterByEmailUseCase registerByEmailUseCase(
            UserRepositoryImpl userRepository,
            BcryptPasswordHasher passwordHasher,
            UuidUserIdGenerator userIdGenerator,
            FakeEmailService emailService,
            JwtTokenProvider tokenGenerator,
            VerificationTokenRepositoryImpl tokenRepo, 
            WebPresenter presenter
    ) {
        return new RegisterByEmailUseCase(
                userRepository,
                passwordHasher,
                userIdGenerator,
                emailService,
                () -> "fake-secure-token-" + System.currentTimeMillis(), 
                tokenRepo, // Đã có repo thật
                presenter
        );
    }

    @Bean
    public LoginByEmailUseCase loginByEmailUseCase(
            UserRepositoryImpl userRepository,
            BcryptPasswordHasher passwordHasher,
            JwtTokenProvider tokenGenerator,
            WebPresenter presenter
    ) {
        return new LoginByEmailUseCase(userRepository, passwordHasher, tokenGenerator, presenter);
    }

    @Bean
    public VerifyEmailUseCase verifyEmailUseCase(
            UserRepositoryImpl userRepository,
            VerificationTokenRepositoryImpl tokenRepo,
            WebPresenter presenter
    ) {
        return new VerifyEmailUseCase(userRepository, tokenRepo, presenter);
    }
}