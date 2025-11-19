package adapters.ManageUser.SearchUsers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import usecase.ManageUser.UserData;
import usecase.ManageUser.SearchUsers.PaginationData;
import usecase.ManageUser.SearchUsers.SearchUsersOutputBoundary;
import usecase.ManageUser.SearchUsers.SearchUsersResponseData;

public class SearchUsersPresenter implements SearchUsersOutputBoundary{
	private SearchUsersViewModel viewModel;

    public SearchUsersPresenter(SearchUsersViewModel viewModel) {
        this.viewModel = viewModel;
    }
	    
	@Override
	public void present(SearchUsersResponseData responseData) {
		// 1. Map các trường chung
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        // 2. Xử lý DTO lồng nhau
        if (responseData.success) {
            // Biên dịch List<UserData> -> List<UserSummaryViewDTO>
            viewModel.users = responseData.users.stream()
                .map(this::mapUserToViewDTO)
                .collect(Collectors.toList());
            
            // Biên dịch PaginationData -> PaginationViewDTO
            viewModel.pagination = mapPaginationToViewDTO(responseData.pagination);
        } else {
            viewModel.users = Collections.emptyList();
            viewModel.pagination = null;
        }
	}

	/**
     * Hàm helper "biên dịch" UserData (logic)
     * sang UserSummaryViewDTO (hiển thị, all strings).
     */
    private UserSummaryViewDTO mapUserToViewDTO(UserData data) {
        UserSummaryViewDTO dto = new UserSummaryViewDTO();
        dto.id = data.userId;
        dto.email = data.email;
        dto.fullName = data.firstName + " " + data.lastName;
        dto.role = String.valueOf(data.role); // Enum -> String
        dto.status = String.valueOf(data.status); // Enum -> String
        return dto;
    }
    
    /**
     * Hàm helper "biên dịch" PaginationData (logic)
     * sang PaginationViewDTO (hiển thị, all strings).
     */
    private PaginationViewDTO mapPaginationToViewDTO(PaginationData data) {
        PaginationViewDTO dto = new PaginationViewDTO();
        dto.totalCount = String.valueOf(data.totalCount);
        dto.currentPage = String.valueOf(data.currentPage);
        dto.pageSize = String.valueOf(data.pageSize);
        dto.totalPages = String.valueOf(data.totalPages);
        return dto;
    }

    /**
     * Controller sẽ gọi hàm này để lấy ViewModel.
     */
    public SearchUsersViewModel getModel() {
        return this.viewModel;
    }
}
