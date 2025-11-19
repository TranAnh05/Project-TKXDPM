package usecase.ManageUser.RequestPasswordReset;

import Entities.User;
import usecase.ManageUser.IEmailService;
import usecase.ManageUser.IPasswordHasher;
import usecase.ManageUser.IPasswordResetTokenIdGenerator;
import usecase.ManageUser.IPasswordResetTokenRepository;
import usecase.ManageUser.ISecureTokenGenerator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;

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
