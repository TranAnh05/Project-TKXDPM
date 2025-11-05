package adapters.SearchUsers;

import java.util.ArrayList;
import java.util.List;

import adapters.ManageUser.UserViewDTO;
import application.dtos.ManageUser.UserOutputData;
import application.dtos.SearchUsers.SearchUsersOutputData;
import application.ports.out.SearchUsers.SearchUsersOutputBoundary;

public class SearchUsersPresenter implements SearchUsersOutputBoundary{
	private SearchUsersViewModel viewModel;

    public SearchUsersPresenter(SearchUsersViewModel viewModel) { this.viewModel = viewModel; }

    public SearchUsersViewModel getViewModel() { return this.viewModel; }

	@Override
	public void present(SearchUsersOutputData output) {
		List<UserViewDTO> viewDTOs = new ArrayList<>();
        if (output.users != null) {
            for (UserOutputData userData : output.users) {
                viewDTOs.add(mapToViewDTO(userData));
            }
        }
        viewModel.success = String.valueOf(output.success);
        viewModel.message = output.message;
        viewModel.users = viewDTOs;
		
	}

	private UserViewDTO mapToViewDTO(UserOutputData data) {
		UserViewDTO dto = new UserViewDTO();
		
        dto.id = String.valueOf(data.id);
        dto.email = data.email;
        dto.fullName = data.fullName;	
        dto.address = data.address;
        dto.role = data.role.name(); // Enum -> String
        dto.isBlocked = String.valueOf(data.isBlocked); // boolean -> String
        
        return dto;
	}

}
