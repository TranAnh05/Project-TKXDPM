package application.dtos.ManageUser.UpdateUserRole;

import usecase.ManageUser.UserOutputData;

public class UpdateUserRoleOutputData {
	public boolean success;
    public String message;
    public UserOutputData updatedUser;
}
