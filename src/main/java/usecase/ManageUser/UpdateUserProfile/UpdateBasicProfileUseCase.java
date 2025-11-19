package usecase.ManageUser.UpdateUserProfile;

import Entities.User;
import usecase.ManageUser.IAuthTokenValidator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;

public class UpdateBasicProfileUseCase extends AbstractUpdateUserProfileUseCase{

	public UpdateBasicProfileUseCase(IAuthTokenValidator tokenValidator, IUserRepository userRepository,
			UpdateUserProfileOutputBoundary outputBoundary) {
		super(tokenValidator, userRepository, outputBoundary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void applyUpdatesToData(UserData userData, UpdateUserProfileRequestData input) {
		userData.firstName = input.firstName;
        userData.lastName = input.lastName;
        userData.phoneNumber = (input.phoneNumber == null || input.phoneNumber.trim().isEmpty()) 
	                               ? null 
	                               : input.phoneNumber.trim();
	}

	@Override
	protected void validateSpecificUpdates(UpdateUserProfileRequestData input) {
		// TODO Auto-generated method stub
		User.validateName(input.firstName, input.lastName);
	    User.validatePhoneNumber(input.phoneNumber);
	}

}
