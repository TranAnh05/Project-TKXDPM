package cgx.com.adapters.ManageUser.AdminViewUserDetails;

import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserResponseData;
import cgx.com.usecase.ManageUser.AdminViewUserDetails.AdminViewUserDetailsOutputBoundary;

public class AdminViewUserDetailsPresenter implements AdminViewUserDetailsOutputBoundary{
	private AdminViewUserDetailsViewModel viewModel;

    public AdminViewUserDetailsPresenter(AdminViewUserDetailsViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
	@Override
	public void present(AdminUpdateUserResponseData responseData) {
		// 1. Map các trường chung
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        // 2. Xử lý DTO lồng nhau
        AdminManagedUserViewDTO viewDTO = null;
        if (responseData.success) {
            // Chỉ tạo DTO con nếu thành công
            viewDTO = mapToViewDTO(responseData);
        }
        
        viewModel.userProfile = viewDTO;
	}

	private AdminManagedUserViewDTO mapToViewDTO(AdminUpdateUserResponseData data) {
		AdminManagedUserViewDTO dto = new AdminManagedUserViewDTO();
        dto.id = data.userId;
        dto.email = data.email;
        dto.firstName = data.firstName;
        dto.lastName = data.lastName;
        dto.phoneNumber = (data.phoneNumber == null) ? "" : data.phoneNumber;
        dto.role = String.valueOf(data.role);
        dto.status = String.valueOf(data.status);
        return dto;
	}
	
	public AdminViewUserDetailsViewModel getModel() {
        return this.viewModel;
    }
}
