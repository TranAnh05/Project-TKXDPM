package cgx.com.usecase.ManageUser.AuthenticateUser;

import cgx.com.Entities.User;
import cgx.com.usecase.ManageUser.IAuthTokenGenerator;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class LoginByEmailUseCase extends AbstractAuthenticateUserUseCase{

	public LoginByEmailUseCase(IUserRepository userRepository, IPasswordHasher passwordHasher,
			IAuthTokenGenerator tokenGenerator, AuthenticateUserOutputBoundary outputBoundary) {
		super(userRepository, passwordHasher, tokenGenerator, outputBoundary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void validateAuthenticationTypeSpecific(AuthenticateUserRequestData input)
			throws IllegalArgumentException {
		User.validatePassword(input.password);
	}

	@Override
	protected void authenticate(AuthenticateUserRequestData input, UserData userData) throws SecurityException {
		boolean isPasswordValid = passwordHasher.verify(
	            input.password,       
	            userData.hashedPassword 
        );
		
		if (!isPasswordValid) {
            throw new SecurityException("Sai thông tin đăng nhập.");
        }
	}
}
