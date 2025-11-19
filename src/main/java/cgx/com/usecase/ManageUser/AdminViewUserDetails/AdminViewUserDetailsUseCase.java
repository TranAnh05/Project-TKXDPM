package cgx.com.usecase.ManageUser.AdminViewUserDetails;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserResponseData;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public class AdminViewUserDetailsUseCase implements AdminViewUserDetailsInputBoundary {
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final AdminViewUserDetailsOutputBoundary outputBoundary;

    public AdminViewUserDetailsUseCase(IAuthTokenValidator tokenValidator,
                                       IUserRepository userRepository,
                                       AdminViewUserDetailsOutputBoundary outputBoundary) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.outputBoundary = outputBoundary;
    }
    
	@Override
    public final void execute(AdminViewUserDetailsRequestData input) {
        // Sử dụng DTO "logic" của Admin
        AdminUpdateUserResponseData output = new AdminUpdateUserResponseData();

        try {
            // 1. Kiểm tra input (Token)
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }
            if (input.targetUserId == null || input.targetUserId.trim().isEmpty()) {
                throw new IllegalArgumentException("Phải cung cấp ID người dùng mục tiêu (targetUserId).");
            }

            // 2. Xác thực Token & Phân quyền (Authorization)
            AuthPrincipal adminPrincipal = tokenValidator.validate(input.authToken);
            if (adminPrincipal.role != UserRole.ADMIN) {
                throw new SecurityException("Không có quyền truy cập.");
            }

            // 3. Tìm User Data mục tiêu từ CSDL (Chung)
            UserData targetUserData = userRepository.findByUserId(input.targetUserId);
            if (targetUserData == null) {
                throw new SecurityException("Không tìm thấy người dùng mục tiêu.");
            }
            
            // 4. Báo cáo thành công (Map UserData -> AdminUserResponseData)
            output = mapToResponseData(targetUserData);

        } catch (IllegalArgumentException e) {
            // 5. BẮT LỖI VALIDATION
            output.success = false;
            output.message = e.getMessage();
        } catch (SecurityException e) {
            // 6. BẮT LỖI NGHIỆP VỤ / BẢO MẬT (T3)
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // 7. Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

        // 8. Trình bày kết quả (Chung)
        outputBoundary.present(output);
    }

	private AdminUpdateUserResponseData mapToResponseData(UserData data) {
		AdminUpdateUserResponseData output = new AdminUpdateUserResponseData();
		
        output.success = true;
        output.message = "Lấy thông tin thành công.";
        output.userId = data.userId;
        output.email = data.email;
        output.firstName = data.firstName;
        output.lastName = data.lastName;
        output.phoneNumber = data.phoneNumber;
        output.role = data.role;
        output.status = data.status;
        
        return output;
	}

}
