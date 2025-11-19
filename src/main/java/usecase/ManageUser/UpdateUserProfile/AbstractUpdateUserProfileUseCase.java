package usecase.ManageUser.UpdateUserProfile;

import java.time.Instant;

import usecase.ManageUser.IAuthTokenValidator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;
import usecase.ManageUser.ViewUserProfile.AuthPrincipal;
import usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

public abstract class AbstractUpdateUserProfileUseCase implements UpdateUserProfileInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final UpdateUserProfileOutputBoundary outputBoundary;

    public AbstractUpdateUserProfileUseCase(IAuthTokenValidator tokenValidator,
                                            IUserRepository userRepository,
                                            UpdateUserProfileOutputBoundary outputBoundary) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.outputBoundary = outputBoundary;
    }
    
    @Override
    public final void execute(UpdateUserProfileRequestData input) {
        // Dùng chung Response DTO với 'ViewProfile'
        ViewUserProfileResponseData output = new ViewUserProfileResponseData();

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

            // 4. Validate dữ liệu mới (Riêng - Concrete)
            // Lớp Con sẽ validate các trường input mới
            validateSpecificUpdates(input);

            // 5. Áp dụng thay đổi (Riêng - Concrete)
            // Lớp Con sẽ thay đổi DTO
            applyUpdatesToData(userData, input);
            
            // 6. Cập nhật thời gian và Lưu CSDL (Chung)
            userData.updatedAt = Instant.now();
            UserData savedData = userRepository.update(userData);

            // 7. Báo cáo thành công (Chung)
            output.success = true;
            output.message = "Cập nhật hồ sơ thành công.";
            output.userId = savedData.userId;
            output.email = savedData.email;
            output.firstName = savedData.firstName;
            output.lastName = savedData.lastName;
            output.phoneNumber = savedData.phoneNumber;

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

	protected abstract void applyUpdatesToData(UserData userData, UpdateUserProfileRequestData input);

	protected abstract void validateSpecificUpdates(UpdateUserProfileRequestData input);
}
