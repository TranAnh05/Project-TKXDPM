package adapters.ManageUser.BlockUser;

import adapters.ManageUser.UserViewDTO;
import application.dtos.ManageUser.BlockUser.BlockUserOutputData;
import application.ports.out.ManageUser.BlockUser.BlockUserOutputBoundary;
import usecase.ManageUser.UserOutputData;

public class BlockUserPresenter implements BlockUserOutputBoundary{
	private BlockUserViewModel viewModel;

    public BlockUserPresenter(BlockUserViewModel viewModel) { this.viewModel = viewModel; }

    public BlockUserViewModel getViewModel() { return this.viewModel; }

	@Override
	public void present(BlockUserOutputData output) {
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
