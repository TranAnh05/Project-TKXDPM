package usecase.ManageUser.AdminViewUserDetails;

import usecase.ManageUser.AdminUpdateUser.AdminUpdateUserResponseData;

public interface AdminViewUserDetailsOutputBoundary {
	void present(AdminUpdateUserResponseData responseData);
}
