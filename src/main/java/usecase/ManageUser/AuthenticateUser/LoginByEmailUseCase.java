package usecase.ManageUser.AuthenticateUser;

import usecase.ManageUser.IAuthTokenGenerator;
import usecase.ManageUser.IPasswordHasher;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;

public class LoginByEmailUseCase extends AbstractAuthenticateUserUseCase{

	public LoginByEmailUseCase(IUserRepository userRepository, IPasswordHasher passwordHasher,
			IAuthTokenGenerator tokenGenerator, AuthenticateUserOutputBoundary outputBoundary) {
		super(userRepository, passwordHasher, tokenGenerator, outputBoundary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void validateAuthenticationTypeSpecific(AuthenticateUserRequestData input)
			throws IllegalArgumentException {
		if (input.password == null || input.password.isEmpty()) {
	            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
	}

	@Override
	protected void authenticate(AuthenticateUserRequestData input, UserData userData) throws SecurityException {
		// Logic xác thực "riêng": So sánh mật khẩu
		boolean isPasswordValid = passwordHasher.verify(
	            input.password,       // Mật khẩu thô
	            userData.hashedPassword // Mật khẩu băm từ CSDL
        );
		
		if (!isPasswordValid) {
            // Quy tắc bảo mật: Thông báo chung
            throw new SecurityException("Sai thông tin đăng nhập.");
        }
	}
}
