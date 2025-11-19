package usecase.ManageUser.UpdateUserProfile;

import usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

public interface UpdateUserProfileOutputBoundary {
	 void present(ViewUserProfileResponseData responseData);
}
