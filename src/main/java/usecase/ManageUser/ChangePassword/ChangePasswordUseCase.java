package usecase.ManageUser.ChangePassword;

import Entities.User;
import usecase.ManageUser.IAuthTokenValidator;
import usecase.ManageUser.IPasswordHasher;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;

public class ChangePasswordUseCase extends AbstractChangePasswordUseCase{

	public ChangePasswordUseCase(IAuthTokenValidator tokenValidator, IUserRepository userRepository,
			IPasswordHasher passwordHasher, ChangePasswordOutputBoundary outputBoundary) {
		super(tokenValidator, userRepository, passwordHasher, outputBoundary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void applyPasswordChange(UserData userData, ChangePasswordRequestData input) {
		// Băm mật khẩu mới
        String newHashedPassword = this.passwordHasher.hash(input.newPassword);
        
        // Gán vào DTO (Entity instance method
        // `changePassword` không dùng được ở đây
        // vì chúng ta đang thao tác trên UserData DTO)
        userData.hashedPassword = newHashedPassword;	
	}

	@Override
	protected void validateCredentials(ChangePasswordRequestData input, UserData userData) {
		// 1. Kiểm tra mật khẩu cũ
        boolean isOldPasswordValid = this.passwordHasher.verify(
            input.oldPassword,
            userData.hashedPassword
        );
        
        
        if (!isOldPasswordValid) {
            throw new SecurityException("Mật khẩu cũ không chính xác.");
        }
        
        // 2. Validate mật khẩu mới (dùng hàm static của Entity)
        User.validatePassword(input.newPassword);
        
        // 3. Quy tắc nghiệp vụ: Không cho phép Cũ == Mới
        if (input.oldPassword.equals(input.newPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu cũ.");
        }
	}
}
