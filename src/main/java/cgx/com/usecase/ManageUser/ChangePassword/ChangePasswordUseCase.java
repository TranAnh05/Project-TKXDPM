package cgx.com.usecase.ManageUser.ChangePassword;

import java.time.Instant;

import cgx.com.Entities.User;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IEmailService;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class ChangePasswordUseCase implements ChangePasswordInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final IPasswordHasher passwordHasher; // Cần để verify và hash
    protected final ChangePasswordOutputBoundary outputBoundary;
    protected final IEmailService emailService;

    public ChangePasswordUseCase(IAuthTokenValidator tokenValidator,
                                         IUserRepository userRepository,
                                         IPasswordHasher passwordHasher,
                                         ChangePasswordOutputBoundary outputBoundary, 
                                         IEmailService emailService) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.outputBoundary = outputBoundary;
        this.emailService = emailService;
    }
    
    @Override
    public final void execute(ChangePasswordRequestData input) {
        ChangePasswordResponseData output = new ChangePasswordResponseData();

        try {
            // Xác thực Token (Chung)
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            
            // kiểm tra đầu vào
            User.validatePassword(input.oldPassword);
            User.validatePassword(input.newPassword);
           
            // Lấy User từ CSDL
            UserData userData = userRepository.findByUserId(principal.userId);
            User userEntity = mapToEntity(userData);
            
            boolean isOldPasswordValid = passwordHasher.verify(
                    input.oldPassword,
                    userEntity.getHashedPassword()
                );
                
            if (!isOldPasswordValid) {
                throw new SecurityException("Mật khẩu cũ không chính xác.");
            }
            
            if (input.oldPassword.equals(input.newPassword)) {
                throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu cũ.");
            }
            
            String newHashedPassword = passwordHasher.hash(input.newPassword);
            userEntity.changePassword(newHashedPassword);
            
            UserData dataToUpdate = mapToData(userEntity);
            userRepository.update(dataToUpdate); 
            
            emailService.sendPasswordChangeAlert(userEntity.getEmail(), userEntity.getFirstName());

            output.success = true;
            output.message = "Đổi mật khẩu thành công.";

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
        }

        outputBoundary.present(output);
    }

	private UserData mapToData(User entity) {
		return new UserData(
	            entity.getUserId(), entity.getEmail(), entity.getHashedPassword(),
	            entity.getFirstName(), entity.getLastName(), entity.getPhoneNumber(),
	            entity.getRole(), entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt()
	        );
	}

	private User mapToEntity(UserData data) {
		return new User(
	            data.userId, data.email, data.hashedPassword, data.firstName, data.lastName,
	            data.phoneNumber, data.role, data.status, data.createdAt, data.updatedAt
	        );
	}
}
