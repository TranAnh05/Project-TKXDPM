package adapters.ManageUser.UpdateUserRole;

import adapters.ManageUser.UserViewDTO;
import application.dtos.ManageUser.UpdateUserRole.UpdateUserRoleOutputData;
import application.ports.out.ManageUser.UpdateUserRole.UpdateUserRoleOutputBoundary;
import usecase.ManageUser.UserOutputData;

public class UpdateUserRolePresenter implements UpdateUserRoleOutputBoundary{
	private UpdateUserRoleViewModel viewModel;

    public UpdateUserRolePresenter(UpdateUserRoleViewModel viewModel) { this.viewModel = viewModel; }

    public UpdateUserRoleViewModel getViewModel() { return this.viewModel; }
    
	@Override
	public void present(UpdateUserRoleOutputData output) {
		UserViewDTO viewDTO = null;
        if (output.updatedUser != null) {
            viewDTO = mapToViewDTO(output.updatedUser);
        }
        viewModel.success = String.valueOf(output.success);
        viewModel.message = output.message;
        viewModel.updatedUser = viewDTO;
		
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
