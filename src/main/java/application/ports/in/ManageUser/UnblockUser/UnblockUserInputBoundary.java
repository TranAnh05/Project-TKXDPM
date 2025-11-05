package application.ports.in.ManageUser.UnblockUser;

import application.dtos.ManageUser.UnblockUser.UnblockUserInputData;

public interface UnblockUserInputBoundary {
	void execute(UnblockUserInputData input);
}
