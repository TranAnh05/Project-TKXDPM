package application.ports.in.ManageUser.BlockUser;

import application.dtos.ManageUser.BlockUser.BlockUserInputData;

public interface BlockUserInputBoundary {
	void execute(BlockUserInputData input);
}
