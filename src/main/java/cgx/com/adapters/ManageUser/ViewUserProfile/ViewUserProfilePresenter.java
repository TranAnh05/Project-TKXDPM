package cgx.com.adapters.ManageUser.ViewUserProfile;

import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileOutputBoundary;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

public class ViewUserProfilePresenter implements ViewUserProfileOutputBoundary{
	 private ViewUserProfileViewModel viewModel;

    public ViewUserProfilePresenter(ViewUserProfileViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
	@Override
	public void present(ViewUserProfileResponseData responseData) {
		// 1. Map các trường chung
        viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        // 2. Xử lý DTO lồng nhau
        UserProfileViewDTO viewDTO = null;
        if (responseData.success) {
            // Chỉ tạo DTO con nếu thành công
            viewDTO = mapToViewDTO(responseData);
        }
        
        viewModel.userProfile = viewDTO;
	}

	private UserProfileViewDTO mapToViewDTO(ViewUserProfileResponseData data) {
		UserProfileViewDTO dto = new UserProfileViewDTO();
        dto.id = data.userId;
        dto.email = data.email;
        dto.firstName = data.firstName;
        dto.lastName = data.lastName;
        // Đảm bảo không bao giờ trả về null cho UI
        dto.phoneNumber = (data.phoneNumber == null) ? "" : data.phoneNumber;
        return dto;
	}
	
	public ViewUserProfileViewModel getModel() {
        return this.viewModel;
    }
}
