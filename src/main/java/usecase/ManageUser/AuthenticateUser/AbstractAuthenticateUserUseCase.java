package usecase.ManageUser.AuthenticateUser;

import Entities.AccountStatus;
import Entities.User;
import usecase.ManageUser.IAuthTokenGenerator;
import usecase.ManageUser.IPasswordHasher;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;

/**
 * Lớp Use Case TRỪU TƯỢNG (Abstract) cho việc Đăng nhập.
 * Chứa logic chung cho mọi kiểu đăng nhập (ví dụ: kiểm tra tài khoản,
 * tạo token, xử lý lỗi chung).
 */
public abstract class AbstractAuthenticateUserUseCase implements AuthenticateUserInputBoundary{
	protected final IUserRepository userRepository;
    protected final IPasswordHasher passwordHasher;
    protected final IAuthTokenGenerator tokenGenerator;
    protected final AuthenticateUserOutputBoundary outputBoundary;

    public AbstractAuthenticateUserUseCase(IUserRepository userRepository,
                                           IPasswordHasher passwordHasher,
                                           IAuthTokenGenerator tokenGenerator,
                                           AuthenticateUserOutputBoundary outputBoundary) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
        this.outputBoundary = outputBoundary;
    }
    
    @Override
    public final void execute(AuthenticateUserRequestData input) {
        AuthenticateUserResponseData output = new AuthenticateUserResponseData();

        try {
            // 1. Validate chung (Tầng 4 - Entity)
            User.validateEmail(input.email);

            // 2. Validate riêng (Tầng 3 - ConCRETE)
            // (Hàm này là 'abstract' - lớp Con sẽ tự định nghĩa)
            // Ví dụ: LoginByEmail sẽ validate password không rỗng
            validateAuthenticationTypeSpecific(input);

            // 3. Kiểm tra nghiệp vụ (Tầng 3 - Chung)
            UserData userData = userRepository.findByEmail(input.email);
            
            // Quy tắc bảo mật: Không phân biệt "không tìm thấy" hay "sai pass"
            if (userData == null) {
                throw new SecurityException("Sai thông tin đăng nhập.");
            }

            // 4. XÁC THỰC (Tầng 3 - ConCRETE)
            // (Hàm này là 'abstract' - lớp Con sẽ so sánh mật khẩu)
            authenticate(input, userData);

            // 5. Kiểm tra trạng thái tài khoản (Tầng 3 - Chung)
            // Quy tắc nghiệp vụ: Chỉ tài khoản ACTIVE mới được đăng nhập
            if (userData.status != AccountStatus.ACTIVE) {
                throw new SecurityException("Tài khoản đã bị khóa hoặc chưa kích hoạt.");
            }
            
            // 6. TẠO TOKEN (Tầng 3 - Chung)
            String token = tokenGenerator.generate(userData.userId, userData.email, userData.role);

            // 7. Báo cáo thành công (Chung)
            output.success = true;
            output.message = "Đăng nhập thành công!";
            output.token = token;
            output.userId = userData.userId;
            output.email = userData.email;
            output.role = userData.role;

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
            e.printStackTrace();
        }

        // 11. Trình bày kết quả (Chung)
        outputBoundary.present(output);
    }
    
    /**
     * Lớp Con (Concrete) phải implement hàm này để validate
     * các trường riêng của kiểu đăng nhập đó.
     */
    protected abstract void validateAuthenticationTypeSpecific(AuthenticateUserRequestData input) throws IllegalArgumentException;

    /**
     * Lớp Con (Concrete) phải implement hàm này để
     * thực hiện việc xác thực (ví dụ: so sánh mật khẩu).
     */
    protected abstract void authenticate(AuthenticateUserRequestData input, UserData userData) throws SecurityException;
}
