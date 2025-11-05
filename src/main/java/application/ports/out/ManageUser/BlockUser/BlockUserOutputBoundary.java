package application.ports.out.ManageUser.BlockUser;

import application.dtos.ManageUser.BlockUser.BlockUserOutputData;

public interface BlockUserOutputBoundary {
	void present(BlockUserOutputData output);
}
