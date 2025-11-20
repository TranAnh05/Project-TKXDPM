package cgx.com.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import cgx.com.adapters.ManageCategory.AddNewCategory.AddCategoryPresenter;
import cgx.com.adapters.ManageCategory.AddNewCategory.AddCategoryViewModel;
import cgx.com.adapters.ManageCategory.DeleteCategory.DeleteCategoryPresenter;
import cgx.com.adapters.ManageCategory.DeleteCategory.DeleteCategoryViewModel;
import cgx.com.adapters.ManageCategory.UpdateCategory.UpdateCategoryPresenter;
import cgx.com.adapters.ManageCategory.UpdateCategory.UpdateCategoryViewModel;
import cgx.com.adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesPresenter;
import cgx.com.adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesViewModel;
import cgx.com.adapters.ManageUser.AdminCreatedUser.AdminCreateUserPresenter;
import cgx.com.adapters.ManageUser.AdminCreatedUser.AdminCreateUserViewModel;
import cgx.com.adapters.ManageUser.AdminUpdateUser.AdminUpdateUserPresenter;
import cgx.com.adapters.ManageUser.AdminUpdateUser.AdminUpdateUserViewModel;
import cgx.com.adapters.ManageUser.AdminViewUserDetails.AdminViewUserDetailsPresenter;
import cgx.com.adapters.ManageUser.AdminViewUserDetails.AdminViewUserDetailsViewModel;
import cgx.com.adapters.ManageUser.AuthencicateUser.AuthenticateUserPresenter;
import cgx.com.adapters.ManageUser.AuthencicateUser.AuthenticateUserViewModel;
import cgx.com.adapters.ManageUser.ChangePassword.ChangePasswordPresenter;
import cgx.com.adapters.ManageUser.ChangePassword.ChangePasswordViewModel;
import cgx.com.adapters.ManageUser.DeleteUser.DeleteUserPresenter;
import cgx.com.adapters.ManageUser.DeleteUser.DeleteUserViewModel;
import cgx.com.adapters.ManageUser.RegisterUser.RegisterUserPresenter;
import cgx.com.adapters.ManageUser.RegisterUser.RegisterUserViewModel;
import cgx.com.adapters.ManageUser.RequestPasswordReset.RequestPasswordResetPresenter;
import cgx.com.adapters.ManageUser.RequestPasswordReset.RequestPasswordResetViewModel;
import cgx.com.adapters.ManageUser.SearchUsers.SearchUsersPresenter;
import cgx.com.adapters.ManageUser.SearchUsers.SearchUsersViewModel;
import cgx.com.adapters.ManageUser.UpdateUserProfile.UpdateUserProfilePresenter;
import cgx.com.adapters.ManageUser.UpdateUserProfile.UpdateUserProfileViewModel;
import cgx.com.adapters.ManageUser.VerifyPasswordReset.VerifyPasswordResetPresenter;
import cgx.com.adapters.ManageUser.VerifyPasswordReset.VerifyPasswordResetViewModel;
import cgx.com.adapters.ManageUser.ViewUserProfile.ViewUserProfilePresenter;
import cgx.com.adapters.ManageUser.ViewUserProfile.ViewUserProfileViewModel;
import cgx.com.usecase.ManageCategory.ICategoryIdGenerator;
import cgx.com.usecase.ManageCategory.ICategoryRepository;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryInputBoundary;
import cgx.com.usecase.ManageCategory.AddNewCategory.AddCategoryUseCase;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryInputBoundary;
import cgx.com.usecase.ManageCategory.DeleteCategory.DeleteCategoryUseCase;
import cgx.com.usecase.ManageCategory.UpdateCategory.UpdateCategoryInputBoundary;
import cgx.com.usecase.ManageCategory.UpdateCategory.UpdateCategoryUseCase;
import cgx.com.usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesInputBoundary;
import cgx.com.usecase.ManageCategory.ViewAllCategories.ViewAllCategoriesUseCase;
import cgx.com.usecase.ManageUser.IAuthTokenGenerator;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IEmailService;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IPasswordResetTokenIdGenerator;
import cgx.com.usecase.ManageUser.IPasswordResetTokenRepository;
import cgx.com.usecase.ManageUser.ISecureTokenGenerator;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserInputBoundary;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserUseCase;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateFullProfileUseCase;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserInputBoundary;
import cgx.com.usecase.ManageUser.AdminViewUserDetails.AdminViewUserDetailsInputBoundary;
import cgx.com.usecase.ManageUser.AdminViewUserDetails.AdminViewUserDetailsUseCase;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserInputBoundary;
import cgx.com.usecase.ManageUser.AuthenticateUser.LoginByEmailUseCase;
import cgx.com.usecase.ManageUser.ChangePassword.ChangePasswordInputBoundary;
import cgx.com.usecase.ManageUser.ChangePassword.ChangePasswordUseCase;
import cgx.com.usecase.ManageUser.DeleteUser.DeleteUserInputBoundary;
import cgx.com.usecase.ManageUser.DeleteUser.SoftDeleteUserUseCase;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterByEmailUseCase;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserInputBoundary;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserOutputBoundary;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestPasswordResetInputBoundary;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestResetByEmailUseCase;
import cgx.com.usecase.ManageUser.SearchUsers.SearchUsersByNameOrEmailUseCase;
import cgx.com.usecase.ManageUser.SearchUsers.SearchUsersInputBoundary;
import cgx.com.usecase.ManageUser.UpdateUserProfile.UpdateBasicProfileUseCase;
import cgx.com.usecase.ManageUser.UpdateUserProfile.UpdateUserProfileInputBoundary;
import cgx.com.usecase.ManageUser.VerifyPasswordReset.VerifyPasswordResetInputBoundary;
import cgx.com.usecase.ManageUser.VerifyPasswordReset.VerifyResetByTokenUseCase;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewOwnProfileUseCase;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileInputBoundary;

/**
 * MENTOR NOTE:
 * Đây là nơi "đấu dây" (Wiring).
 * Chúng ta khởi tạo các Use Case (Layer 3) bằng cách tiêm (inject)
 * các Implementation (Layer 2) vào constructor của chúng.
 */
@Configuration
public class UseCaseConfiguration {

	// =========================================================================
    // 1. GUEST & AUTHENTICATION
    // =========================================================================

    // --- Register ---
    @Bean
    @RequestScope
    public RegisterUserPresenter registerUserPresenter() {
        return new RegisterUserPresenter(new RegisterUserViewModel());
    }

    @Bean
    @RequestScope
    public RegisterUserInputBoundary registerUserUseCase(
            IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            IUserIdGenerator userIdGenerator,
            RegisterUserPresenter presenter
    ) {
        return new RegisterByEmailUseCase(userRepository, passwordHasher, userIdGenerator, presenter);
    }

    // --- Login (ĐÂY LÀ PHẦN BẠN ĐANG THIẾU) ---
    @Bean
    @RequestScope
    public AuthenticateUserPresenter loginPresenter() {
        return new AuthenticateUserPresenter(new AuthenticateUserViewModel());
    }

    @Bean
    @RequestScope
    public AuthenticateUserInputBoundary loginUseCase(
            IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            IAuthTokenGenerator tokenGenerator,
            AuthenticateUserPresenter presenter
    ) {
        return new LoginByEmailUseCase(userRepository, passwordHasher, tokenGenerator, presenter);
    }

    // =========================================================================
    // 2. CUSTOMER (USER)
    // =========================================================================

    // --- View Profile ---
    @Bean
    @RequestScope
    public ViewUserProfilePresenter viewProfilePresenter() {
        return new ViewUserProfilePresenter(new ViewUserProfileViewModel());
    }

    @Bean
    @RequestScope
    public ViewUserProfileInputBoundary viewProfileUseCase(
            IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            ViewUserProfilePresenter presenter
    ) {
        return new ViewOwnProfileUseCase(tokenValidator, userRepository, presenter);
    }

    // --- Update Profile ---
    @Bean
    @RequestScope
    public UpdateUserProfilePresenter updateProfilePresenter() {
        return new UpdateUserProfilePresenter(new UpdateUserProfileViewModel());
    }

    @Bean
    @RequestScope
    public UpdateUserProfileInputBoundary updateProfileUseCase(
            IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            UpdateUserProfilePresenter presenter
    ) {
        return new UpdateBasicProfileUseCase(tokenValidator, userRepository, presenter);
    }

    // --- Change Password ---
    @Bean
    @RequestScope
    public ChangePasswordPresenter changePasswordPresenter() {
        return new ChangePasswordPresenter(new ChangePasswordViewModel());
    }

    @Bean
    @RequestScope
    public ChangePasswordInputBoundary changePasswordUseCase(
            IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            ChangePasswordPresenter presenter
    ) {
        return new ChangePasswordUseCase(tokenValidator, userRepository, passwordHasher, presenter);
    }

    // =========================================================================
    // 3. ADMIN
    // =========================================================================

    // --- Search Users ---
    @Bean
    @RequestScope
    public SearchUsersPresenter searchUsersPresenter() {
        return new SearchUsersPresenter(new SearchUsersViewModel());
    }

    @Bean
    @RequestScope
    public SearchUsersInputBoundary searchUsersUseCase(
            IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            SearchUsersPresenter presenter
    ) {
        return new SearchUsersByNameOrEmailUseCase(tokenValidator, userRepository, presenter);
    }

    // --- Create New User (By Admin) ---
    @Bean
    @RequestScope
    public AdminCreateUserPresenter adminCreateUserPresenter() {
        return new AdminCreateUserPresenter(new AdminCreateUserViewModel());
    }

    @Bean
    @RequestScope
    public AdminCreateUserInputBoundary adminCreateUserUseCase(
            IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            IUserIdGenerator userIdGenerator,
            AdminCreateUserPresenter presenter
    ) {
        return new AdminCreateUserUseCase(tokenValidator, userRepository, passwordHasher, userIdGenerator, presenter);
    }

    // --- View User Details (By Admin) ---
    @Bean
    @RequestScope
    public AdminViewUserDetailsPresenter adminViewDetailPresenter() {
        return new AdminViewUserDetailsPresenter(new AdminViewUserDetailsViewModel());
    }

    @Bean
    @RequestScope
    public AdminViewUserDetailsInputBoundary adminViewDetailUseCase(
            IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            AdminViewUserDetailsPresenter presenter
    ) {
        return new AdminViewUserDetailsUseCase(tokenValidator, userRepository, presenter);
    }

    // --- Update User (By Admin) ---
    @Bean
    @RequestScope
    public AdminUpdateUserPresenter adminUpdateUserPresenter() {
        return new AdminUpdateUserPresenter(new AdminUpdateUserViewModel());
    }

    @Bean
    @RequestScope
    public AdminUpdateUserInputBoundary adminUpdateUserUseCase(
            IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            AdminUpdateUserPresenter presenter
    ) {
        return new AdminUpdateFullProfileUseCase(tokenValidator, userRepository, presenter);
    }

    // --- Delete User ---
    @Bean
    @RequestScope
    public DeleteUserPresenter deleteUserPresenter() {
        return new DeleteUserPresenter(new DeleteUserViewModel());
    }

    @Bean
    @RequestScope
    public DeleteUserInputBoundary deleteUserUseCase(
            IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            DeleteUserPresenter presenter
    ) {
        return new SoftDeleteUserUseCase(tokenValidator, userRepository, presenter);
    }
    
 // --- UC-7: Request Password Reset ---
    @Bean
    @RequestScope
    public RequestPasswordResetPresenter requestResetPresenter() {
        return new RequestPasswordResetPresenter(new RequestPasswordResetViewModel());
    }

    @Bean
    @RequestScope
    public RequestPasswordResetInputBoundary requestResetUseCase(
            IUserRepository userRepository,
            IPasswordResetTokenRepository tokenRepository,
            IPasswordResetTokenIdGenerator tokenIdGenerator, // Dùng UuidGenerator
            ISecureTokenGenerator tokenGenerator,            // Dùng UuidGenerator
            IPasswordHasher passwordHasher,
            RequestPasswordResetPresenter presenter,
            IEmailService emailService
    ) {
        return new RequestResetByEmailUseCase(
            userRepository, tokenRepository, tokenIdGenerator, tokenGenerator, 
            passwordHasher, presenter, emailService
        );
    }

    // --- UC-8: Verify Password Reset ---
    @Bean
    @RequestScope
    public VerifyPasswordResetPresenter verifyResetPresenter() {
        return new VerifyPasswordResetPresenter(new VerifyPasswordResetViewModel());
    }

    @Bean
    @RequestScope
    public VerifyPasswordResetInputBoundary verifyResetUseCase(
            IUserRepository userRepository,
            IPasswordResetTokenRepository tokenRepository,
            IPasswordHasher passwordHasher,
            VerifyPasswordResetPresenter presenter
    ) {
        return new VerifyResetByTokenUseCase(userRepository, tokenRepository, passwordHasher, presenter);
    }
    
 // =========================================================================
    // 4. PRODUCT TYPE (CATEGORY) MANAGEMENT
    // =========================================================================

    // UC-1: Add Category
    @Bean
    @RequestScope
    public AddCategoryPresenter addCategoryPresenter() {
        return new AddCategoryPresenter(new AddCategoryViewModel());
    }

    @Bean
    @RequestScope
    public AddCategoryInputBoundary addCategoryUseCase(
            ICategoryRepository categoryRepository,
            IAuthTokenValidator tokenValidator,
            ICategoryIdGenerator idGenerator, // Dùng UuidGenerator
            AddCategoryPresenter presenter
    ) {
        return new AddCategoryUseCase(categoryRepository, tokenValidator, idGenerator, presenter);
    }

    // UC-2: Update Category
    @Bean
    @RequestScope
    public UpdateCategoryPresenter updateCategoryPresenter() {
        return new UpdateCategoryPresenter(new UpdateCategoryViewModel());
    }

    @Bean
    @RequestScope
    public UpdateCategoryInputBoundary updateCategoryUseCase(
            ICategoryRepository categoryRepository,
            IAuthTokenValidator tokenValidator,
            UpdateCategoryPresenter presenter
    ) {
        return new UpdateCategoryUseCase(categoryRepository, tokenValidator, presenter);
    }

    // UC-3: Delete Category
    @Bean
    @RequestScope
    public DeleteCategoryPresenter deleteCategoryPresenter() {
        return new DeleteCategoryPresenter(new DeleteCategoryViewModel());
    }

    @Bean
    @RequestScope
    public DeleteCategoryInputBoundary deleteCategoryUseCase(
            ICategoryRepository categoryRepository,
            IAuthTokenValidator tokenValidator,
            DeleteCategoryPresenter presenter
    ) {
        return new DeleteCategoryUseCase(categoryRepository, tokenValidator, presenter);
    }

    // UC-4: View All Categories
    @Bean
    @RequestScope
    public ViewAllCategoriesPresenter viewAllCategoriesPresenter() {
        return new ViewAllCategoriesPresenter(new ViewAllCategoriesViewModel());
    }

    @Bean
    @RequestScope
    public ViewAllCategoriesInputBoundary viewAllCategoriesUseCase(
            ICategoryRepository categoryRepository,
            ViewAllCategoriesPresenter presenter
    ) {
        return new ViewAllCategoriesUseCase(categoryRepository, presenter);
    }
}
