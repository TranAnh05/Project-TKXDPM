package cgx.com.usecase.ManageUser.RequestPasswordReset;

import cgx.com.Entities.User;
import cgx.com.usecase.Interface_Common.IEmailService;
import cgx.com.usecase.Interface_Common.IPasswordHasher;
import cgx.com.usecase.Interface_Common.IPasswordResetTokenIdGenerator;
import cgx.com.usecase.Interface_Common.IPasswordResetTokenRepository;
import cgx.com.usecase.Interface_Common.ISecureTokenGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class RequestResetByEmailUseCase extends AbstractRequestPasswordResetUseCase<EmailResetRequest>{
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
	protected UserData findUser(EmailResetRequest input) {
		 return this.userRepository.findByEmail(input.email);
	}

	@Override
	protected void validateInput(EmailResetRequest input) {
		User.validateEmail(input.email);
	}

}
