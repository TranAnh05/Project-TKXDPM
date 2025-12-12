package cgx.com.usecase.ManageUser.VerifyEmail;

import java.time.Instant;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.User;
import cgx.com.Entities.VerificationToken;
import cgx.com.usecase.Interface_Common.IVerificationTokenRepository;
import cgx.com.usecase.Interface_Common.VerificationTokenData;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class VerifyEmailUseCase implements VerifyEmailInputBoundary {

    private final IUserRepository userRepository;
    private final IVerificationTokenRepository tokenRepository;
    private final VerifyEmailOutputBoundary outputBoundary;

    public VerifyEmailUseCase(IUserRepository userRepository,
                              IVerificationTokenRepository tokenRepository,
                              VerifyEmailOutputBoundary outputBoundary) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(VerifyEmailRequestData input) {
        VerifyEmailResponseData output = new VerifyEmailResponseData();

        try {
            // 1. Validate Input
            VerificationToken.validateToken(input.token);

            // 2. Tìm Token trong DB
            VerificationTokenData tokenData = tokenRepository.findByToken(input.token);
            if (tokenData == null) {
                throw new IllegalArgumentException("Mã xác thực không hợp lệ hoặc không tồn tại.");
            }

            // 3. Kiểm tra hết hạn
            if (tokenData.expiryDate.isBefore(Instant.now())) {
                throw new IllegalArgumentException("Mã xác thực đã hết hạn. Vui lòng yêu cầu gửi lại.");
            }

            // 4. Tìm User tương ứng với Token
            UserData userData = userRepository.findByUserId(tokenData.userId);
            
            User userEntity = mapDataToEntity(userData);
            userEntity.activate();
            
            UserData dataToUpdate = mapEntityToData(userEntity);
            userRepository.update(dataToUpdate);
            
            output.success = true;
            output.message = "Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.";
            // 6. Dọn dẹp: Xóa token đã sử dụng (để không dùng lại được)
            tokenRepository.deleteByToken(input.token);
        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Lỗi hệ thống: " + e.getMessage();
            e.printStackTrace();
        }

        outputBoundary.present(output);
    }

	private UserData mapEntityToData(User entity) {
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

	private User mapDataToEntity(UserData data) {
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
}