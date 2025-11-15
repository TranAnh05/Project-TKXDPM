package application.dtos.ManageUser.BlockUser;

import usecase.ManageUser.UserOutputData;

public class BlockUserOutputData {
	public boolean success;
    public String message;
    public UserOutputData updatedUser;
}
