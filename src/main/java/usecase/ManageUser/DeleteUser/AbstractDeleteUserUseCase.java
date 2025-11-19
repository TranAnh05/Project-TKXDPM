package usecase.ManageUser.DeleteUser;

import java.time.Instant;

import Entities.UserRole;
import usecase.ManageUser.IAuthTokenValidator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;
import usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public abstract class AbstractDeleteUserUseCase implements DeleteUserInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final DeleteUserOutputBoundary outputBoundary;

    public AbstractDeleteUserUseCase(IAuthTokenValidator tokenValidator,
                                     IUserRepository userRepository,
                                     DeleteUserOutputBoundary outputBoundary) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.outputBoundary = outputBoundary;
    }
    
    @Override
    public final void execute(DeleteUserRequestData input) {
        DeleteUserResponseData output = new DeleteUserResponseData();

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
            
            // 3. Quy tắc nghiệp vụ (Admin không thể tự xóa chính mình)
            if (adminPrincipal.userId.equals(input.targetUserId)) {
                throw new SecurityException("Admin không thể tự xóa chính mình.");
            }

            // 4. Tìm User Data mục tiêu từ CSDL (Chung)
            UserData targetUserData = userRepository.findByUserId(input.targetUserId);
            if (targetUserData == null) {
                throw new SecurityException("Không tìm thấy người dùng mục tiêu.");
            }

            // 5. Áp dụng Xóa mềm (Riêng - Concrete)
            applySoftDelete(targetUserData);
            
            // 6. Cập nhật thời gian và Lưu CSDL (Chung)
            targetUserData.updatedAt = Instant.now();
            UserData savedData = userRepository.update(targetUserData); // Dùng lại .update()

            // 7. Báo cáo thành công (Chung)
            output.success = true;
            output.message = "Xóa người dùng thành công.";
            output.deletedUserId = savedData.userId;
            output.newStatus = String.valueOf(savedData.status); // "DELETED"

        } catch (SecurityException e) {
            // 8. BẮT LỖI NGHIỆP VỤ / BẢO MẬT (T3)
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // 9. Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

        // 10. Trình bày kết quả (Chung)
        outputBoundary.present(output);
    }

	protected abstract void applySoftDelete(UserData targetUserData);
}
