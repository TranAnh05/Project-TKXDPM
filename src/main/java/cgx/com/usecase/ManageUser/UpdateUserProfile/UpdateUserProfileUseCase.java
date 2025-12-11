package cgx.com.usecase.ManageUser.UpdateUserProfile;

import java.time.Instant;

import cgx.com.Entities.User;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

public class UpdateUserProfileUseCase implements UpdateUserProfileInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final UpdateUserProfileOutputBoundary outputBoundary;

    public UpdateUserProfileUseCase(IAuthTokenValidator tokenValidator,
                                            IUserRepository userRepository,
                                            UpdateUserProfileOutputBoundary outputBoundary) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.outputBoundary = outputBoundary;
    }
    
    @Override
    public final void execute(UpdateUserProfileRequestData input) {
        ViewUserProfileResponseData output = new ViewUserProfileResponseData();

        try {
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            
            User.validateName(input.firstName, input.lastName);
    	    User.validatePhoneNumber(input.phoneNumber);
            
            UserData userData = userRepository.findByUserId(principal.userId);

            User userEntity = mapToEntity(userData);
            userEntity.updateProfile(input.firstName, input.lastName, input.phoneNumber);
    	    
            UserData userToSave = mapToData(userEntity);
            UserData savedData = userRepository.update(userToSave);

            output.success = true;
            output.message = "Cập nhật hồ sơ thành công.";
            output.userId = savedData.userId;
            output.email = savedData.email;
            output.firstName = savedData.firstName;
            output.lastName = savedData.lastName;
            output.phoneNumber = savedData.phoneNumber;

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
