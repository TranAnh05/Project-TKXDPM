package cgx.com.adapters.ManageUser.DeleteUser;

import cgx.com.usecase.ManageUser.DeleteUser.DeleteUserOutputBoundary;
import cgx.com.usecase.ManageUser.DeleteUser.DeleteUserResponseData;

public class DeleteUserPresenter implements DeleteUserOutputBoundary{
	private DeleteUserViewModel viewModel;

    public DeleteUserPresenter(DeleteUserViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
	@Override
	public void present(DeleteUserResponseData responseData) {
		// 1. Map các trường chung
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        // 2. Xử lý DTO lồng nhau
        DeletedUserViewDTO viewDTO = null;
        if (responseData.success) {
            // Chỉ tạo DTO con nếu thành công
            viewDTO = mapToViewDTO(responseData);
        }
        
        viewModel.deletedUser = viewDTO;
	}

	private DeletedUserViewDTO mapToViewDTO(DeleteUserResponseData data) {
		DeletedUserViewDTO dto = new DeletedUserViewDTO();
        dto.id = data.deletedUserId;
        dto.status = data.newStatus; 
        return dto;
	}
	
	 public DeleteUserViewModel getModel() {
	        return this.viewModel;
	 }

}
