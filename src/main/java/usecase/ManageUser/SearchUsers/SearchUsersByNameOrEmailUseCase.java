package usecase.ManageUser.SearchUsers;

import usecase.ManageUser.IAuthTokenValidator;
import usecase.ManageUser.IUserRepository;

public class SearchUsersByNameOrEmailUseCase extends AbstractSearchUsersUseCase{

	public SearchUsersByNameOrEmailUseCase(IAuthTokenValidator tokenValidator, IUserRepository userRepository,
			SearchUsersOutputBoundary outputBoundary) {
		super(tokenValidator, userRepository, outputBoundary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected UserSearchCriteria buildSearchCriteria(SearchUsersRequestData input) {
		return new UserSearchCriteria(input.searchTerm);
	}

}
