package cgx.com.adapters.ManageUser.AdminCreatedUser;

import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserOutputBoundary;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserResponseData;

public class AdminCreateUserPresenter implements AdminCreateUserOutputBoundary{
	private AdminCreateUserViewModel viewModel;

    public AdminCreateUserPresenter(AdminCreateUserViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
	@Override
	public void present(AdminCreateUserResponseData responseData) {
		viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        // 2. Xử lý DTO lồng nhau
        AdminCreatedUserViewDTO viewDTO = null;
        if (responseData.success) {
            // Chỉ tạo DTO con nếu thành công
            viewDTO = mapToViewDTO(responseData);
        }
        
        viewModel.createdUser = viewDTO;
	}

	private AdminCreatedUserViewDTO mapToViewDTO(AdminCreateUserResponseData data) {
		AdminCreatedUserViewDTO dto = new AdminCreatedUserViewDTO();
        dto.id = data.createdUserId;
        dto.email = data.email;
        dto.fullName = data.fullName;
        dto.role = String.valueOf(data.role);
        dto.status = String.valueOf(data.status);
        return dto;
	}

	public AdminCreateUserViewModel getModel() {
		return this.viewModel;
	}

}
