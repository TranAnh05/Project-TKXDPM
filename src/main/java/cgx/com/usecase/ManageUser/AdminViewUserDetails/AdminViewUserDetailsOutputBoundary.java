package cgx.com.usecase.ManageUser.AdminViewUserDetails;

import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserResponseData;

public interface AdminViewUserDetailsOutputBoundary {
	void present(AdminUpdateUserResponseData responseData);
}
