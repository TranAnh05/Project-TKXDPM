package cgx.com.usecase.ManageUser.AdminUpdateUser;

import java.time.Instant;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class AdminUpdateUserUseCase implements AdminUpdateUserInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final AdminUpdateUserOutputBoundary outputBoundary;

    public AdminUpdateUserUseCase(IAuthTokenValidator tokenValidator,
                                          IUserRepository userRepository,
                                          AdminUpdateUserOutputBoundary outputBoundary) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.outputBoundary = outputBoundary;
    }
    
    @Override
    public final void execute(AdminUpdateUserRequestData input) {
    	AdminUpdateUserResponseData output = new AdminUpdateUserResponseData();

        try {
            // Xác thực Token & Phân quyền
            AuthPrincipal adminPrincipal = tokenValidator.validate(input.authToken);
            User.validateIsAdmin(adminPrincipal.role);

            UserData userAdminData = userRepository.findByUserId(adminPrincipal.userId);
            UserData targetUserData = userRepository.findByUserId(input.targetUserId);
            if (targetUserData == null) {
                throw new SecurityException("Không tìm thấy người dùng mục tiêu.");
            }
            
            User userAdminEntity = mapToEntity(userAdminData);
            // Admin không tự sửa chính mình
            userAdminEntity.validateAdminSelfUpdate(input.targetUserId);
            
            // Kiểm tra dữ liệu đầu vào
            User.validateEmail(input.email);
            User.validateName(input.firstName, input.lastName);
            User.validatePhoneNumber(input.phoneNumber);
            User.validateRole(input.role);
            User.validateStatus(input.status);
            
            // Kiểm tra email mới có tồn tại chưa
            if (!input.email.equalsIgnoreCase(targetUserData.email)) {
                if (userRepository.findByEmail(input.email) != null) {
                    throw new IllegalArgumentException("Email mới này đã tồn tại.");
                }
            }
            
            User userTargetEntity = mapToEntity(targetUserData);
            
            // Gán dữ liệu mới
            userTargetEntity.setUserId(input.targetUserId);
            userTargetEntity.setEmail(input.email);
            userTargetEntity.setFirstName(input.firstName);
            userTargetEntity.setLastName(input.lastName);
            userTargetEntity.setPhoneNumber(input.phoneNumber);
            userTargetEntity.setRole(UserRole.valueOf(input.role));
            userTargetEntity.setStatus(AccountStatus.valueOf(input.status));
            userTargetEntity.touch();

            UserData userToSave = mapToData(userTargetEntity);
            
            UserData savedData = userRepository.update(userToSave);

            output.success = true;
            output.message = "Cập nhật người dùng thành công.";
            output.userId = userTargetEntity.getUserId();
            output.email = userTargetEntity.getEmail();
            output.firstName = userTargetEntity.getFirstName();
            output.lastName = userTargetEntity.getLastName();
            output.phoneNumber = userTargetEntity.getPhoneNumber();
            
            // CÁC TRƯỜNG MỚI CỦA ADMIN
            output.role = userTargetEntity.getRole();
            output.status = userTargetEntity.getStatus();

        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

        outputBoundary.present(output);
    }

	private UserData mapToData(User user) {
		return new UserData(
	            user.getUserId(),
	            user.getEmail(),
	            user.getHashedPassword(),
	            user.getFirstName(),
	            user.getLastName(),
	            user.getPhoneNumber(),
	            user.getRole(),
	            user.getStatus(),
	            user.getCreatedAt(),
	            user.getUpdatedAt()
	        );
	}

	private User mapToEntity(UserData user) {
		return new User(
				user.userId,
				user.email,
				user.hashedPassword,
				user.firstName,
				user.lastName,
				user.phoneNumber,
				user.role,
				user.status,
				user.createdAt,
				user.updatedAt
		);
	}
}
