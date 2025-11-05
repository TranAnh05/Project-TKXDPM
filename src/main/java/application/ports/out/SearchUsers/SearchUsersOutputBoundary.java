package application.ports.out.SearchUsers;

import application.dtos.SearchUsers.SearchUsersOutputData;

public interface SearchUsersOutputBoundary {
	void present(SearchUsersOutputData output);
}
