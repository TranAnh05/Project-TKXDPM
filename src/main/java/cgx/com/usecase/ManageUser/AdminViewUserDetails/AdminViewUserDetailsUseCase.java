package cgx.com.usecase.ManageUser.AdminViewUserDetails;

import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserResponseData;

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
        AdminUpdateUserResponseData output = new AdminUpdateUserResponseData();

        try {
            AuthPrincipal adminPrincipal = tokenValidator.validate(input.authToken);
            User.validateIsAdmin(adminPrincipal.role);
            
            UserData targetUserData = userRepository.findByUserId(input.targetUserId);
            if (targetUserData == null) {
                throw new SecurityException("Không tìm thấy tài khoản người dùng.");
            }
            
            output = mapToResponseData(targetUserData);

        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

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
