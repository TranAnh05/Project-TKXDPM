package cgx.com.adapters.ManageUser.SearchUsers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import cgx.com.usecase.Interface_Common.PaginationData;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.SearchUsers.SearchUsersOutputBoundary;
import cgx.com.usecase.ManageUser.SearchUsers.SearchUsersResponseData;

public class SearchUsersPresenter implements SearchUsersOutputBoundary{
	private SearchUsersViewModel viewModel;

    public SearchUsersPresenter(SearchUsersViewModel viewModel) {
        this.viewModel = viewModel;
    }
	    
	@Override
	public void present(SearchUsersResponseData responseData) {
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        if (responseData.success) {
            viewModel.users = responseData.users.stream()
                .map(this::mapUserToViewDTO)
                .collect(Collectors.toList());
            
            viewModel.pagination = mapPaginationToViewDTO(responseData.pagination);
        } else {
            viewModel.users = Collections.emptyList();
            viewModel.pagination = null;
        }
	}
	
    private UserSummaryViewDTO mapUserToViewDTO(UserData data) {
        UserSummaryViewDTO dto = new UserSummaryViewDTO();
        dto.id = data.userId;
        dto.email = data.email;
        dto.fullName = data.firstName + " " + data.lastName;
        dto.role = String.valueOf(data.role); 
        dto.status = String.valueOf(data.status); 
        return dto;
    }
    
    private PaginationViewDTO mapPaginationToViewDTO(PaginationData data) {
        PaginationViewDTO dto = new PaginationViewDTO();
        dto.totalCount = String.valueOf(data.totalCount);
        dto.currentPage = String.valueOf(data.currentPage);
        dto.pageSize = String.valueOf(data.pageSize);
        dto.totalPages = String.valueOf(data.totalPages);
        return dto;
    }
    
    public SearchUsersViewModel getModel() {
        return this.viewModel;
    }
}
