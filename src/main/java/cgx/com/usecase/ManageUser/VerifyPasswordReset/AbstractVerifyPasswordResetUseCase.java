package cgx.com.usecase.ManageUser.VerifyPasswordReset;

import java.time.Instant;

import cgx.com.Entities.User;
import cgx.com.usecase.Interface_Common.IPasswordHasher;
import cgx.com.usecase.Interface_Common.IPasswordResetTokenRepository;
import cgx.com.usecase.Interface_Common.PasswordResetTokenData;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public abstract class AbstractVerifyPasswordResetUseCase<T extends BaseVerifyResetRequestData> implements VerifyPasswordResetInputBoundary<T>{
	protected final IUserRepository userRepository;
    protected final IPasswordResetTokenRepository tokenRepository;
    protected final IPasswordHasher passwordHasher;
    protected final VerifyPasswordResetOutputBoundary outputBoundary;

    public AbstractVerifyPasswordResetUseCase(IUserRepository userRepository,
                                              IPasswordResetTokenRepository tokenRepository,
                                              IPasswordHasher passwordHasher,
                                              VerifyPasswordResetOutputBoundary outputBoundary) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordHasher = passwordHasher;
        this.outputBoundary = outputBoundary;
    }
    
    @Override
    public final void execute(T input) {
        VerifyPasswordResetResponseData output = new VerifyPasswordResetResponseData();

        try {
            validateInput(input);
            
            PasswordResetTokenData tokenData = findAndValidateToken(input);

            // Tìm User Data từ CSDL
            UserData userData = userRepository.findByUserId(tokenData.userId);
            
            User userEntity = mapToEntity(userData);
            
            // Băm mật khẩu mới
            String newHashedPassword = passwordHasher.hash(input.newPassword);
            
            // nghiệp vụ đổi mật khẩu
            userEntity.changePassword(newHashedPassword);
            
            UserData dataToUpdate = mapToData(userEntity);
            userRepository.update(dataToUpdate);
            
            // Xóa token
            tokenRepository.deleteByTokenId(tokenData.tokenId);

            output.success = true;
            output.message = "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập ngay bây giờ.";

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
            entity.getUserId(),
            entity.getEmail(),
            entity.getHashedPassword(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getPhoneNumber(),
            entity.getRole(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
	}

	private User mapToEntity(UserData data) {
		return new User(
	            data.userId,
	            data.email,
	            data.hashedPassword,
	            data.firstName,
	            data.lastName,
	            data.phoneNumber,
	            data.role,     
	            data.status,   
	            data.createdAt,
	            data.updatedAt
	        );
	}

	protected abstract PasswordResetTokenData findAndValidateToken(T input);

	protected abstract void validateInput(T input);
}
