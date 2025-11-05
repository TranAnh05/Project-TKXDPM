package application.ports.in.ManageUser.UpdateUserRole;

import application.dtos.ManageUser.UpdateUserRole.UpdateUserRoleInputData;

public interface UpdateUserRoleInputBoundary {
	void execute(UpdateUserRoleInputData input);
}
