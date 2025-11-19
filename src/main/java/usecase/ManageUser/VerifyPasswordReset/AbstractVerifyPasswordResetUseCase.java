package usecase.ManageUser.VerifyPasswordReset;

import java.time.Instant;

import usecase.ManageUser.IPasswordHasher;
import usecase.ManageUser.IPasswordResetTokenRepository;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.PasswordResetTokenData;
import usecase.ManageUser.UserData;

public abstract class AbstractVerifyPasswordResetUseCase implements VerifyPasswordResetInputBoundary{
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
    public final void execute(VerifyPasswordResetRequestData input) {
        VerifyPasswordResetResponseData output = new VerifyPasswordResetResponseData();

        try {
            // 1. Validate input (Riêng - Concrete)
            // (Ví dụ: validate mật khẩu mới)
            validateInput(input);
            
            // 2. Tìm và Xác thực Token (Riêng - Concrete)
            // (Tìm token, check hết hạn, ...)
            PasswordResetTokenData tokenData = findAndValidateToken(input);

            // 3. Tìm User Data từ CSDL (Chung)
            UserData userData = userRepository.findByUserId(tokenData.userId);
            if (userData == null) {
                // Rất hiếm khi xảy ra (token tồn tại nhưng user không)
                throw new SecurityException("Token không hợp lệ hoặc đã hết hạn.");
            }
            
            // 4. Băm mật khẩu mới (Chung)
            String newHashedPassword = passwordHasher.hash(input.newPassword);
            
            // 5. Cập nhật User (Chung)
            userData.hashedPassword = newHashedPassword;
            userData.updatedAt = Instant.now();
            userRepository.update(userData);
            
            // 6. Xóa Token (Chung)
            // Chúng ta nên xóa token, nhưng ngay cả khi việc này thất bại,
            // nghiệp vụ vẫn được coi là thành công vì mật khẩu ĐÃ được đổi.
            try {
                tokenRepository.deleteByTokenId(tokenData.tokenId);
            } catch (Exception e) {
                // Log lỗi này (ví dụ: "Không thể xóa token đã sử dụng: " + tokenData.tokenId)
                // Nhưng KHÔNG ném lỗi cho người dùng.
            	System.out.println("Không thể xóa token đã sử dụng: " + tokenData.tokenId);
            }

            // 7. Báo cáo thành công (Chung)
            output.success = true;
            output.message = "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập ngay bây giờ.";

        } catch (IllegalArgumentException e) {
            // 8. BẮT LỖI VALIDATION (T4)
            output.success = false;
            output.message = e.getMessage();
        } catch (SecurityException e) {
            // 9. BẮT LỖI NGHIỆP VỤ / BẢO MẬT (T3)
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // 10. Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

        // 11. Trình bày kết quả (Chung)
        outputBoundary.present(output);
    }

	protected abstract PasswordResetTokenData findAndValidateToken(VerifyPasswordResetRequestData input);

	protected abstract void validateInput(VerifyPasswordResetRequestData input);
}
