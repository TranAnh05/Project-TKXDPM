package cgx.com.usecase.ManageUser.DeleteUser;

import java.time.Instant;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.User;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class SoftDeleteUserUseCase extends AbstractDeleteUserUseCase{

	public SoftDeleteUserUseCase(IAuthTokenValidator tokenValidator, IUserRepository userRepository,
			DeleteUserOutputBoundary outputBoundary) {
		super(tokenValidator, userRepository, outputBoundary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void applySoftDelete(UserData targetUserData) {
		// 1. "Tái tạo" (Re-hydrate) Entity `User` từ DTO `UserData`
        User userEntity = mapDataToEntity(targetUserData);

        // 2. Gọi logic nghiệp vụ của Entity (Layer 4)
        // Use Case (Layer 3) không cần biết chi tiết
        // của việc "xóa mềm" là gì.
        userEntity.softDelete();

        // 3. "Làm phẳng" (De-hydrate) các thay đổi từ Entity
        // trở lại DTO để chuẩn bị lưu.
        mapEntityToData(userEntity, targetUserData);
	}

	private void mapEntityToData(User entity, UserData dataToUpdate) {
		dataToUpdate.email = entity.getEmail();
        dataToUpdate.firstName = entity.getFirstName();
        dataToUpdate.lastName = entity.getLastName();
        dataToUpdate.phoneNumber = entity.getPhoneNumber();
        dataToUpdate.hashedPassword = entity.getHashedPassword();
        dataToUpdate.status = entity.getStatus();
	}

	private User mapDataToEntity(UserData data) {
		return new User(
	            data.userId,
	            data.email,
	            data.hashedPassword,
	            data.firstName,
	            data.lastName,
	            data.phoneNumber,
	            data.role,
	            data.status,
	            data.createdAt,
	            data.updatedAt
	        );
	}
}
