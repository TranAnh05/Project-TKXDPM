package adapters.ManageUser.UpdateUserProfile;

import adapters.ManageUser.ViewUserProfile.UserProfileViewDTO;
import usecase.ManageUser.UpdateUserProfile.UpdateUserProfileOutputBoundary;
import usecase.ManageUser.ViewUserProfile.ViewUserProfileResponseData;

public class UpdateUserProfilePresenter implements UpdateUserProfileOutputBoundary{
    private UpdateUserProfileViewModel viewModel;

    public UpdateUserProfilePresenter(UpdateUserProfileViewModel viewModel) {
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
        
        viewModel.updatedProfile = viewDTO;
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

	 public UpdateUserProfileViewModel getModel() {
	        return this.viewModel;
     }
}
