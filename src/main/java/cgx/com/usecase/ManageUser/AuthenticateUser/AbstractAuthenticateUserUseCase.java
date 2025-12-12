package cgx.com.usecase.ManageUser.AuthenticateUser;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.User;
import cgx.com.usecase.Interface_Common.IAuthTokenGenerator;
import cgx.com.usecase.Interface_Common.IPasswordHasher;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public abstract class AbstractAuthenticateUserUseCase implements AuthenticateUserInputBoundary{
	protected final IUserRepository userRepository;
    protected final IPasswordHasher passwordHasher;
    protected final IAuthTokenGenerator tokenGenerator;
    protected final AuthenticateUserOutputBoundary outputBoundary;

    public AbstractAuthenticateUserUseCase(IUserRepository userRepository,
                                           IPasswordHasher passwordHasher,
                                           IAuthTokenGenerator tokenGenerator,
                                           AuthenticateUserOutputBoundary outputBoundary) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
        this.outputBoundary = outputBoundary;
    }
    
    @Override
    public final void execute(AuthenticateUserRequestData input) {
        AuthenticateUserResponseData output = new AuthenticateUserResponseData();

        try {
            User.validateEmail(input.email);
            validateAuthenticationTypeSpecific(input);

            UserData userData = userRepository.findByEmail(input.email);
            
            if (userData == null) {
                throw new SecurityException("Không tìm thấy tài khoản.");
            }

            // kiểm tra mật khẩu
            authenticate(input, userData);
            
            User user = mapToEntity(userData);

            // Nghiệp vụ: Chỉ tài khoản ACTIVE mới được đăng nhập
            user.validateLoginStatus();
            user.touch();
            userData.updatedAt = user.getUpdatedAt();
            userRepository.update(userData);
            
            // TẠO TOKEN
            String token = tokenGenerator.generate(userData.userId, userData.email, userData.role);

            output.success = true;
            output.message = "Đăng nhập thành công!";
            output.token = token;
            output.userId = userData.userId;
            output.email = userData.email;
            output.role = userData.role;

        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
            e.printStackTrace();
        }

        outputBoundary.present(output);
    }
    
    private User mapToEntity(UserData userData) {
		return new User(
			userData.userId,
			userData.email,
			userData.hashedPassword,
			userData.firstName,
			userData.lastName,
			userData.phoneNumber,
			userData.role,
			userData.status,
			userData.createdAt,
			userData.updatedAt
		);
	}

    protected abstract void validateAuthenticationTypeSpecific(AuthenticateUserRequestData input) throws IllegalArgumentException;

    protected abstract void authenticate(AuthenticateUserRequestData input, UserData userData) throws SecurityException;
}
