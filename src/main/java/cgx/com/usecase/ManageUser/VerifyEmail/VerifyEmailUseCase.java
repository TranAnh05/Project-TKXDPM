package cgx.com.usecase.ManageUser.VerifyEmail;

import java.time.Instant;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.VerificationToken;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.IVerificationTokenRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.VerificationTokenData;

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

            // 2. Tìm Token trong DB (Kịch bản 2)
            VerificationTokenData tokenData = tokenRepository.findByToken(input.token);
            if (tokenData == null) {
                throw new IllegalArgumentException("Mã xác thực không hợp lệ hoặc không tồn tại.");
            }

            // 3. Kiểm tra hết hạn (Kịch bản 3)
            if (tokenData.expiryDate.isBefore(Instant.now())) {
                // (Tùy chọn: Có thể xóa token hết hạn ở đây luôn để dọn dẹp DB)
                // tokenRepository.deleteByToken(input.token);
                throw new IllegalArgumentException("Mã xác thực đã hết hạn. Vui lòng yêu cầu gửi lại.");
            }

            // 4. Tìm User tương ứng với Token
            UserData userData = userRepository.findByUserId(tokenData.userId);
//            if (userData == null) {
//                // Trường hợp hiếm: Token còn nhưng User đã bị xóa cứng khỏi DB
//                tokenRepository.deleteByToken(input.token); // Dọn rác
//                throw new IllegalArgumentException("Tài khoản không tồn tại.");
//            }

            // 5. Kích hoạt tài khoản (Kịch bản 1 & 4)
            if (userData.status == AccountStatus.ACTIVE) {
                // Kịch bản 4: Đã kích hoạt rồi -> Coi như thành công luôn
                output.success = true;
                output.message = "Tài khoản của bạn đã được kích hoạt trước đó.";
            } else {
                // Kịch bản 1: Kích hoạt lần đầu
                
                // Cập nhật trạng thái DTO
                userData.status = AccountStatus.ACTIVE;
                userData.updatedAt = Instant.now();
                
                // Lưu User xuống DB
                userRepository.update(userData);
                
                output.success = true;
                output.message = "Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.";
            }

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
}