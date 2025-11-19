package usecase.ManageUser.AdminUpdateUser;

import java.time.Instant;

import Entities.UserRole;
import usecase.ManageUser.IAuthTokenValidator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;
import usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public abstract class AbstractAdminUpdateUserUseCase implements AdminUpdateUserInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final AdminUpdateUserOutputBoundary outputBoundary;

    public AbstractAdminUpdateUserUseCase(IAuthTokenValidator tokenValidator,
                                          IUserRepository userRepository,
                                          AdminUpdateUserOutputBoundary outputBoundary) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.outputBoundary = outputBoundary;
    }
    
    @Override
    public final void execute(AdminUpdateUserRequestData input) {
        // Tái sử dụng DTO Output của ViewProfile
    	AdminUpdateUserResponseData output = new AdminUpdateUserResponseData();

        try {
            // 1. Kiểm tra input (Token)
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }

            // 2. Xác thực Token & Phân quyền (Authorization)
            AuthPrincipal adminPrincipal = tokenValidator.validate(input.authToken);
            if (adminPrincipal.role != UserRole.ADMIN) {
                throw new SecurityException("Không có quyền truy cập.");
            }
            
            // 3. Quy tắc nghiệp vụ (Admin không thể tự sửa mình)
            if (adminPrincipal.userId.equals(input.targetUserId)) {
                throw new SecurityException("Admin không thể tự cập nhật vai trò/trạng thái của chính mình. " +
                                            "Vui lòng sử dụng chức năng 'Cập nhật Hồ sơ' thông thường.");
            }

            // 4. Tìm User Data mục tiêu từ CSDL (Chung)
            UserData targetUserData = userRepository.findByUserId(input.targetUserId);
            if (targetUserData == null) {
                throw new SecurityException("Không tìm thấy người dùng mục tiêu.");
            }

            // 5. Validate dữ liệu mới (Riêng - Concrete)
            validateSpecificUpdates(input, targetUserData);

            // 6. Áp dụng thay đổi (Riêng - Concrete)
            applySpecificUpdates(input, targetUserData);
            
            // 7. Cập nhật thời gian và Lưu CSDL (Chung)
            targetUserData.updatedAt = Instant.now();
            UserData savedData = userRepository.update(targetUserData);

            // 8. Báo cáo thành công (Chung)
            output = mapToResponseData(savedData);

        } catch (IllegalArgumentException e) {
            // 9. BẮT LỖI VALIDATION (T4)
            output.success = false;
            output.message = e.getMessage();
        } catch (SecurityException e) {
            // 10. BẮT LỖI NGHIỆP VỤ / BẢO MẬT (T3)
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // 11. Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

        // 12. Trình bày kết quả (Chung)
        outputBoundary.present(output);
    }

	private AdminUpdateUserResponseData mapToResponseData(UserData data) {
		AdminUpdateUserResponseData output = new AdminUpdateUserResponseData();
        output.success = true;
        output.message = "Cập nhật người dùng thành công.";
        output.userId = data.userId;
        output.email = data.email;
        output.firstName = data.firstName;
        output.lastName = data.lastName;
        output.phoneNumber = data.phoneNumber;
        
        // CÁC TRƯỜNG MỚI CỦA ADMIN
        output.role = data.role;
        output.status = data.status;
        
        return output;
	}

	protected abstract void applySpecificUpdates(AdminUpdateUserRequestData input, UserData targetUserData);

	protected abstract void validateSpecificUpdates(AdminUpdateUserRequestData input, UserData targetUserData);
}
