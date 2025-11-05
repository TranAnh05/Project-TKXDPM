package application.ports.out.ManageUser.ViewAllUsers;

import application.dtos.ManageUser.ViewAllUsers.ViewAllUsersOutputData;

public interface ViewAllUsersOutputBoundary {
	void present(ViewAllUsersOutputData output);
}
