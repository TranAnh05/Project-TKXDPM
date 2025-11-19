package infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import adapters.ManageUser.RegisterUser.RegisterUserPresenter;
import adapters.ManageUser.RegisterUser.RegisterUserViewModel;
import usecase.ManageUser.IPasswordHasher;
import usecase.ManageUser.IUserIdGenerator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.RegisterUser.RegisterByEmailUseCase;
import usecase.ManageUser.RegisterUser.RegisterUserInputBoundary;
import usecase.ManageUser.RegisterUser.RegisterUserOutputBoundary;

/**
 * MENTOR NOTE:
 * Đây là nơi "đấu dây" (Wiring).
 * Chúng ta khởi tạo các Use Case (Layer 3) bằng cách tiêm (inject)
 * các Implementation (Layer 2) vào constructor của chúng.
 */
@Configuration
public class UseCaseConfiguration {

    // --- ĐĂNG KÝ (REGISTER) ---

    // 1. Tạo ViewModel (Mỗi request cần 1 instance mới -> @RequestScope)
    @Bean
    @RequestScope
    public RegisterUserViewModel registerUserViewModel() {
        return new RegisterUserViewModel();
    }

    // 2. Tạo Presenter (Cần ViewModel)
    @Bean
    @RequestScope
    public RegisterUserOutputBoundary registerUserPresenter(RegisterUserViewModel viewModel) {
        return new RegisterUserPresenter(viewModel);
    }

    // 3. Tạo Use Case (Cần Repository, Hasher, ID Generator, Presenter)
    @Bean
    @RequestScope // Use Case cũng nên là Request Scope vì Presenter là Request Scope
    public RegisterUserInputBoundary registerUserUseCase(
            IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            IUserIdGenerator userIdGenerator,
            RegisterUserOutputBoundary registerUserPresenter // Inject Bean ở trên vào đây
    ) {
        return new RegisterByEmailUseCase(
                userRepository,
                passwordHasher,
                userIdGenerator,
                registerUserPresenter
        );
    }
    
    // --- (Bạn sẽ làm tương tự cho Login, ViewProfile, v.v. ở đây) ---
}
