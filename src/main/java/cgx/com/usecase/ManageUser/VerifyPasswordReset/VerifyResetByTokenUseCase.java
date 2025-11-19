package cgx.com.usecase.ManageUser.VerifyPasswordReset;

import cgx.com.Entities.PasswordResetToken;
import cgx.com.Entities.User;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IPasswordResetTokenRepository;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.PasswordResetTokenData;

public class VerifyResetByTokenUseCase extends AbstractVerifyPasswordResetUseCase{

	public VerifyResetByTokenUseCase(IUserRepository userRepository, IPasswordResetTokenRepository tokenRepository,
			IPasswordHasher passwordHasher, VerifyPasswordResetOutputBoundary outputBoundary) {
		super(userRepository, tokenRepository, passwordHasher, outputBoundary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected PasswordResetTokenData findAndValidateToken(VerifyPasswordResetRequestData input) {
		// 1. Băm token (plain-text) từ input
        String hashedToken = this.passwordHasher.hash(input.resetToken);
        
        // 2. Tìm token (đã băm) trong CSDL
        PasswordResetTokenData tokenData = this.tokenRepository.findByHashedToken(hashedToken);
        
        // 3. Quy tắc nghiệp vụ: Nếu không tìm thấy
        if (tokenData == null) {
            throw new SecurityException("Token không hợp lệ hoặc đã hết hạn.");
        }
        
        // 4. "Tái tạo" (re-hydrate) Entity để gọi logic nghiệp vụ
        PasswordResetToken tokenEntity = new PasswordResetToken(
            tokenData.tokenId,
            tokenData.hashedToken,
            tokenData.userId,
            tokenData.expiresAt
        );
        
        // 5. Quy tắc nghiệp vụ: Kiểm tra hết hạn (logic của Entity)
        if (tokenEntity.isExpired()) {
            throw new SecurityException("Token không hợp lệ hoặc đã hết hạn.");
        }
        
        // Nếu hợp lệ, trả về DTO
        return tokenData;
	}

	@Override
	protected void validateInput(VerifyPasswordResetRequestData input) {
		 // 1. Kiểm tra token (plain-text) không rỗng
        if (input.resetToken == null || input.resetToken.trim().isEmpty()) {
            throw new SecurityException("Token không hợp lệ hoặc đã hết hạn.");
        }
        
        // 2. Validate mật khẩu mới (dùng hàm static của Entity)
        User.validatePassword(input.newPassword);
	}

}
