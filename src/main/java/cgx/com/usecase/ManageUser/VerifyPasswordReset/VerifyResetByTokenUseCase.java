package cgx.com.usecase.ManageUser.VerifyPasswordReset;

import cgx.com.Entities.PasswordResetToken;
import cgx.com.Entities.User;
import cgx.com.usecase.Interface_Common.IPasswordHasher;
import cgx.com.usecase.Interface_Common.IPasswordResetTokenRepository;
import cgx.com.usecase.Interface_Common.PasswordResetTokenData;
import cgx.com.usecase.ManageUser.IUserRepository;

public class VerifyResetByTokenUseCase extends AbstractVerifyPasswordResetUseCase<VerifyPasswordResetRequestData>{

	public VerifyResetByTokenUseCase(IUserRepository userRepository, IPasswordResetTokenRepository tokenRepository,
			IPasswordHasher passwordHasher, VerifyPasswordResetOutputBoundary outputBoundary) {
		super(userRepository, tokenRepository, passwordHasher, outputBoundary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected PasswordResetTokenData findAndValidateToken(VerifyPasswordResetRequestData input) {
		// Băm token
        String hashedToken = this.passwordHasher.hash(input.resetToken);
        
        // Tìm token (đã băm) trong CSDL
        PasswordResetTokenData tokenData = this.tokenRepository.findByHashedToken(hashedToken);
        
        PasswordResetToken tokenEntity = new PasswordResetToken(
            tokenData.tokenId,
            tokenData.hashedToken,
            tokenData.userId,
            tokenData.expiresAt
        );
        
        tokenEntity.isExpired();
        
        return tokenData;
	}

	@Override
	protected void validateInput(VerifyPasswordResetRequestData input) {
        User.validatePassword(input.newPassword);
	}

}
