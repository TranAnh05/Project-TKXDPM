package application.ports.out.ManageUser.UpdateUserRole;

import application.dtos.ManageUser.UpdateUserRole.UpdateUserRoleOutputData;

public interface UpdateUserRoleOutputBoundary {
	void present(UpdateUserRoleOutputData output);
}
