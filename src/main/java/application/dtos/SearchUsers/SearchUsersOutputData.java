package application.dtos.SearchUsers;

import java.util.List;

import application.dtos.ManageUser.UserOutputData;

public class SearchUsersOutputData {
	public boolean success;
    public String message;
    public List<UserOutputData> users;
}
