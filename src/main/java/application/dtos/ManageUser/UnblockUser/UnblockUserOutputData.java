package application.dtos.ManageUser.UnblockUser;

import usecase.ManageUser.UserOutputData;

public class UnblockUserOutputData {
	public boolean success;
    public String message;
    public UserOutputData updatedUser;
    
    public UnblockUserOutputData() {
    	
    }
}
