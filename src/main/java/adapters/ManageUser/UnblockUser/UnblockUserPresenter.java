package adapters.ManageUser.UnblockUser;

import adapters.ManageUser.UserViewDTO;
import application.dtos.ManageUser.UserOutputData;
import application.dtos.ManageUser.UnblockUser.UnblockUserOutputData;
import application.ports.out.ManageUser.UnblockUser.UnblockUserOutputBoundary;

public class UnblockUserPresenter implements UnblockUserOutputBoundary{
	private UnblockUserViewModel viewModel;

    public UnblockUserPresenter(UnblockUserViewModel viewModel) { this.viewModel = viewModel; }

    public UnblockUserViewModel getViewModel() { return this.viewModel; }

	@Override
	public void present(UnblockUserOutputData output) {
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
        dto.role = String.valueOf(data.role); // Enum -> String
        dto.isBlocked = String.valueOf(data.isBlocked); // boolean -> String
        return dto;
	}

}
