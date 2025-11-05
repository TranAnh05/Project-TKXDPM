package application.ports.in.SearchUsers;

import application.dtos.SearchUsers.SearchUsersInputData;

public interface SearchUsersInputBoundary {
	void execute(SearchUsersInputData input);
}
