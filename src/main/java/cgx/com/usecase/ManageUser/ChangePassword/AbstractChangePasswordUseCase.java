package cgx.com.usecase.ManageUser.ChangePassword;

import java.time.Instant;

import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public abstract class AbstractChangePasswordUseCase implements ChangePasswordInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final IPasswordHasher passwordHasher; // Cần để verify và hash
    protected final ChangePasswordOutputBoundary outputBoundary;

    public AbstractChangePasswordUseCase(IAuthTokenValidator tokenValidator,
                                         IUserRepository userRepository,
                                         IPasswordHasher passwordHasher,
                                         ChangePasswordOutputBoundary outputBoundary) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.outputBoundary = outputBoundary;
    }
    
    @Override
    public final void execute(ChangePasswordRequestData input) {
        ChangePasswordResponseData output = new ChangePasswordResponseData();

        try {
            // 1. Kiểm tra input (Chung)
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }

            // 2. Xác thực Token (Chung)
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            
            // 3. Lấy User Data hiện tại từ CSDL (Chung)
            UserData userData = userRepository.findByUserId(principal.userId);
            if (userData == null) {
                throw new SecurityException("Không tìm thấy người dùng.");
            }

            // 4. Xác thực nghiệp vụ (Riêng - Concrete)
            // Lớp Con sẽ validate mật khẩu cũ và mật khẩu mới
            validateCredentials(input, userData);

            // 5. Áp dụng thay đổi (Riêng - Concrete)
            // Lớp Con sẽ băm và gán mật khẩu mới
            applyPasswordChange(userData, input);
            
            // 6. Cập nhật thời gian và Lưu CSDL (Chung)
            userData.updatedAt = Instant.now();
            userRepository.update(userData); // Dùng lại hàm update

            // 7. Báo cáo thành công (Chung)
            output.success = true;
            output.message = "Đổi mật khẩu thành công.";

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

	protected abstract void applyPasswordChange(UserData userData, ChangePasswordRequestData input);

	protected abstract void validateCredentials(ChangePasswordRequestData input, UserData userData);
}
