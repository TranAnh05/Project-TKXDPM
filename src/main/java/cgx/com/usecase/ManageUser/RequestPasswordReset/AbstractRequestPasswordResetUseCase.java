package cgx.com.usecase.ManageUser.RequestPasswordReset;

import java.time.Duration;

import cgx.com.Entities.PasswordResetToken;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IPasswordResetTokenIdGenerator;
import cgx.com.usecase.ManageUser.IPasswordResetTokenRepository;
import cgx.com.usecase.ManageUser.ISecureTokenGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.PasswordResetTokenData;
import cgx.com.usecase.ManageUser.UserData;

public abstract class AbstractRequestPasswordResetUseCase<T extends ResetRequestData> implements RequestPasswordResetInputBoundary<T>{
	protected final IUserRepository userRepository;
    protected final IPasswordResetTokenRepository tokenRepository;
    protected final IPasswordResetTokenIdGenerator tokenIdGenerator;
    protected final ISecureTokenGenerator tokenGenerator;
    protected final IPasswordHasher passwordHasher;
    protected final RequestPasswordResetOutputBoundary outputBoundary;

    // Thời gian hiệu lực của token (ví dụ: 15 phút)
    private static final Duration TOKEN_VALIDITY = Duration.ofMinutes(15);

    public AbstractRequestPasswordResetUseCase(IUserRepository userRepository,
                                               IPasswordResetTokenRepository tokenRepository,
                                               IPasswordResetTokenIdGenerator tokenIdGenerator,
                                               ISecureTokenGenerator tokenGenerator,
                                               IPasswordHasher passwordHasher,
                                               RequestPasswordResetOutputBoundary outputBoundary) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenIdGenerator = tokenIdGenerator;
        this.tokenGenerator = tokenGenerator;
        this.passwordHasher = passwordHasher;
        this.outputBoundary = outputBoundary;
    }
    
    
    @Override
    public final void execute(T input) {
        RequestPasswordResetResponseData output = new RequestPasswordResetResponseData();

        try {
            validateInput(input);
            
            // Tìm người dùng
            UserData userData = findUser(input);

            if (userData != null) {
                // Tạo token 
                String plainTextToken = tokenGenerator.generate();
                
                // Băm token (để lưu vào CSDL)
                String hashedToken = passwordHasher.hash(plainTextToken);
                
                // Tạo ID cho bản ghi token
                String tokenId = tokenIdGenerator.generate();

                // Tạo entity 
                PasswordResetToken tokenEntity = PasswordResetToken.create(
                        tokenId, userData.userId, hashedToken, TOKEN_VALIDITY
                );

                // Map sang DTO 
                PasswordResetTokenData tokenData = mapEntityToData(tokenEntity);
                
                // Lưu token vào csdl
                tokenRepository.save(tokenData);

                // Gửi thông báo 
                // (Gửi email/SMS chứa plainTextToken)
                sendNotification(userData, plainTextToken);
            }
            
            // LUÔN LUÔN trả về thông báo này (Quy tắc bảo mật)
            output.success = true;
            output.message = "Nếu thông tin của bạn là chính xác và tồn tại trong hệ thống, " +
                             "một hướng dẫn đặt lại mật khẩu đã được gửi.";

        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
            
        } catch (Exception e) {
            // 7. Bắt lỗi hệ thống
            output.success = true;
            output.message = "Nếu thông tin của bạn là chính xác và tồn tại trong hệ thống, " +
                             "một hướng dẫn đặt lại mật khẩu đã được gửi.";
            e.printStackTrace();
        }

        outputBoundary.present(output);
    }


	protected abstract void sendNotification(UserData userData, String plainTextToken);


	private PasswordResetTokenData mapEntityToData(PasswordResetToken entity) {
		return new PasswordResetTokenData(
	            entity.getTokenId(),
	            entity.getHashedToken(),
	            entity.getUserId(),
	            entity.getExpiresAt()
	        );
	}


	protected abstract UserData findUser(T input);

	protected abstract void validateInput(T input);
}
