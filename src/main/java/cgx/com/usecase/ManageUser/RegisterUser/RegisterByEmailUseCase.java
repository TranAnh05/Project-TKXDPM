package cgx.com.usecase.ManageUser.RegisterUser;

import cgx.com.Entities.User;
import cgx.com.usecase.ManageUser.IEmailService;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.ISecureTokenGenerator;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.IVerificationTokenRepository;
import cgx.com.usecase.ManageUser.UserData;

public class RegisterByEmailUseCase extends AbstractRegisterUserUseCase {

	public RegisterByEmailUseCase(IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            IUserIdGenerator userIdGenerator,
            IEmailService emailService,          
            ISecureTokenGenerator tokenGenerator,
            IVerificationTokenRepository verificationTokenRepository,
            RegisterUserOutputBoundary outputBoundary) {
			super(userRepository, passwordHasher, userIdGenerator, emailService, tokenGenerator,verificationTokenRepository, outputBoundary);
	}

    @Override
    protected void validateRegistrationTypeSpecific(RegisterUserRequestData input) throws IllegalArgumentException {
        User.validatePassword(input.password);
    }

    @Override
    protected User createEntity(RegisterUserRequestData input) {
        // 1. Tạo ID mới -> Sử dụng service tạo entity
        String userId = this.userIdGenerator.generate();
        
        // 2. Băm mật khẩu -> Sử dụng service tạo entity
        String hashedPassword = this.passwordHasher.hash(input.password);

        // 3. Gọi Factory của Entity
        return User.createNewCustomer(
            userId,
            input.email,
            hashedPassword,
            input.firstName,
            input.lastName
        );
    }

	@Override
	protected UserData mapEntityToData(User user) {
        return new UserData(
            user.getUserId(),
            user.getEmail(),
            user.getHashedPassword(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getRole(),
            user.getStatus(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
	}

	@Override
	protected void sendActivationEmail(UserData user, String verificationToken) {
		this.emailService.sendVerificationEmail(
	            user.email, 
	            user.firstName + " " + user.lastName, 
	            verificationToken
	        );
	}
}