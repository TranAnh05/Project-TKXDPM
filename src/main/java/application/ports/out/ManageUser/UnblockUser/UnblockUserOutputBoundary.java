package application.ports.out.ManageUser.UnblockUser;

import application.dtos.ManageUser.UnblockUser.UnblockUserOutputData;

public interface UnblockUserOutputBoundary {
	void present(UnblockUserOutputData output);
}
