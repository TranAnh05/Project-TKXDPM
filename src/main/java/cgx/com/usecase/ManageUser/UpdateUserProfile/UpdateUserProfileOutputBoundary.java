package cgx.com.usecase.ManageUser.UpdateUserProfile;

import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

public interface UpdateUserProfileOutputBoundary {
	 void present(ViewUserProfileResponseData responseData);
}
