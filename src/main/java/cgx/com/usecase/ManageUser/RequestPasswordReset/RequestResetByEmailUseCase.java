package cgx.com.usecase.ManageUser.RequestPasswordReset;

import cgx.com.Entities.User;
import cgx.com.usecase.ManageUser.IEmailService;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IPasswordResetTokenIdGenerator;
import cgx.com.usecase.ManageUser.IPasswordResetTokenRepository;
import cgx.com.usecase.ManageUser.ISecureTokenGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class RequestResetByEmailUseCase extends AbstractRequestPasswordResetUseCase{
	// Thêm 1 dependency "riêng": IEmailService
    private final IEmailService emailService;

	public RequestResetByEmailUseCase(IUserRepository userRepository, IPasswordResetTokenRepository tokenRepository,
			IPasswordResetTokenIdGenerator tokenIdGenerator, ISecureTokenGenerator tokenGenerator,
			IPasswordHasher passwordHasher, RequestPasswordResetOutputBoundary outputBoundary, IEmailService emailService) {
		super(userRepository, tokenRepository, tokenIdGenerator, tokenGenerator, passwordHasher, outputBoundary);
		this.emailService = emailService;
	}

	@Override
	protected void sendNotification(UserData userData, String plainTextToken) {
		 String fullName = userData.firstName + " " + userData.lastName;
	        this.emailService.sendPasswordResetEmail(
	            userData.email,
	            fullName,
	            plainTextToken
	        );
	}

	@Override
	protected UserData findUser(RequestPasswordResetRequestData input) {
		 return this.userRepository.findByEmail(input.email);
	}

	@Override
	protected void validateInput(RequestPasswordResetRequestData input) {
		User.validateEmail(input.email);
	}

}
