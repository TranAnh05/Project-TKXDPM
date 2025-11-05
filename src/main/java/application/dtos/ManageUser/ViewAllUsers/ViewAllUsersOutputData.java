package application.dtos.ManageUser.ViewAllUsers;

import java.util.List;

import application.dtos.ManageUser.UserOutputData;

public class ViewAllUsersOutputData {
	public boolean success;
    public String message;
    public List<UserOutputData> users;
}
