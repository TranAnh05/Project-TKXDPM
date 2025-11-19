package usecase.ManageUser.AdminUpdateUser;

import Entities.AccountStatus;
import Entities.User;
import Entities.UserRole;
import usecase.ManageUser.IAuthTokenValidator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;

public class AdminUpdateFullProfileUseCase extends AbstractAdminUpdateUserUseCase {

	public AdminUpdateFullProfileUseCase(IAuthTokenValidator tokenValidator, IUserRepository userRepository,
			AdminUpdateUserOutputBoundary outputBoundary) {
		super(tokenValidator, userRepository, outputBoundary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void applySpecificUpdates(AdminUpdateUserRequestData input, UserData targetUserData) {
		targetUserData.email = input.email;
        targetUserData.firstName = input.firstName;
        targetUserData.lastName = input.lastName;
        targetUserData.phoneNumber = (input.phoneNumber == null || input.phoneNumber.trim().isEmpty()) 
                                     ? null 
                                     : input.phoneNumber.trim();
                                     
        // Chuyển đổi String (đã validate) sang Enum
        targetUserData.role = UserRole.valueOf(input.role.toUpperCase());
        targetUserData.status = AccountStatus.valueOf(input.status.toUpperCase());
	}

	@Override
	protected void validateSpecificUpdates(AdminUpdateUserRequestData input, UserData targetUserData) {
		// 1. Validate các trường cơ bản (dùng hàm static của Entity)
        User.validateName(input.firstName, input.lastName);
        User.validatePhoneNumber(input.phoneNumber);
        User.validateEmail(input.email);
        
        // 2. Validate Role và Status
        User.validateRole(input.role);
        User.validateStatus(input.status);

        // 3. Quy tắc nghiệp vụ: Kiểm tra Email duy nhất (nếu email bị thay đổi)
        if (!input.email.equalsIgnoreCase(targetUserData.email)) {
            // Email đã bị thay đổi -> Kiểm tra xem email mới đã tồn tại chưa
            if (userRepository.findByEmail(input.email) != null) {
                throw new IllegalArgumentException("Email mới này đã tồn tại.");
            }
        }
	}

}
