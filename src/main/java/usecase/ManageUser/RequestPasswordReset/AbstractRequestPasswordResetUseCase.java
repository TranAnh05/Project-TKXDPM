package usecase.ManageUser.RequestPasswordReset;

import java.time.Duration;

import Entities.PasswordResetToken;
import usecase.ManageUser.IPasswordHasher;
import usecase.ManageUser.IPasswordResetTokenIdGenerator;
import usecase.ManageUser.IPasswordResetTokenRepository;
import usecase.ManageUser.ISecureTokenGenerator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.PasswordResetTokenData;
import usecase.ManageUser.UserData;

public abstract class AbstractRequestPasswordResetUseCase implements RequestPasswordResetInputBoundary{
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
    public final void execute(RequestPasswordResetRequestData input) {
        RequestPasswordResetResponseData output = new RequestPasswordResetResponseData();

        try {
            // 1. Validate input (Riêng - Concrete)
            validateInput(input);

            // 2. Tìm người dùng (Riêng - Concrete)
            UserData userData = findUser(input);

            // 3. Logic nghiệp vụ (Chung)
            if (userData != null) {
                // Nếu tìm thấy User, chúng ta MỚI thực hiện các bước tiếp theo
                
                // 3a. Tạo token (plain-text)
                String plainTextToken = tokenGenerator.generate();
                // 3b. Băm token (để lưu CSDL)
                String hashedToken = passwordHasher.hash(plainTextToken);
                // 3c. Tạo ID cho bản ghi token
                String tokenId = tokenIdGenerator.generate();

                // 3d. Tạo Entity (Layer 4)
                PasswordResetToken tokenEntity = PasswordResetToken.create(
                        tokenId, userData.userId, hashedToken, TOKEN_VALIDITY
                );

                // 3e. Map sang DTO (Layer 3)
                PasswordResetTokenData tokenData = mapEntityToData(tokenEntity);
                
                // 3f. Lưu token (đã băm) vào CSDL
                tokenRepository.save(tokenData);

                // 3g. Gửi thông báo (Riêng - Concrete)
                // (Gửi email/SMS chứa plainTextToken)
                sendNotification(userData, plainTextToken);
            }
            
            // 4. (Nếu userData == null, chúng ta không làm gì cả)
            // -> BỎ QUA các bước 3a-3g.

            // 5. Báo cáo thành công (Chung)
            // LUÔN LUÔN trả về thông báo này (Quy tắc bảo mật)
            output.success = true;
            output.message = "Nếu thông tin của bạn là chính xác và tồn tại trong hệ thống, " +
                             "một hướng dẫn đặt lại mật khẩu đã được gửi.";

        } catch (IllegalArgumentException e) {
            // 6. BẮT LỖI VALIDATION (T4)
            // (Ví dụ: Email/SĐT không đúng định dạng)
            output.success = false;
            output.message = e.getMessage();
            
        } catch (Exception e) {
            // 7. Bắt lỗi hệ thống (ví dụ: CSDL sập, Email service sập)
            // Mặc dù là lỗi, nhưng chúng ta vẫn trả về thông báo thành công
            // để đảm bảo an toàn (không tiết lộ lỗi hệ thống).
            // Chúng ta NÊN log lỗi `e` này ra.
            output.success = true;
            output.message = "Nếu thông tin của bạn là chính xác và tồn tại trong hệ thống, " +
                             "một hướng dẫn đặt lại mật khẩu đã được gửi.";
            e.printStackTrace();
        }

        // 8. Trình bày kết quả (Chung)
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


	protected abstract UserData findUser(RequestPasswordResetRequestData input);


	protected abstract void validateInput(RequestPasswordResetRequestData input);
}
