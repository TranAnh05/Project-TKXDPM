package cgx.com.adapters.ManageUser.AuthencicateUser;

import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserOutputBoundary;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserResponseData;

public class AuthenticateUserPresenter implements AuthenticateUserOutputBoundary{
	private AuthenticateUserViewModel viewModel;

    public AuthenticateUserPresenter(AuthenticateUserViewModel viewModel) {
        this.viewModel = viewModel;
    }
	
	@Override
	public void present(AuthenticateUserResponseData responseData) {
		viewModel.success = String.valueOf(responseData.success);
		viewModel.message = responseData.message;
	        
		LoginSuccessViewDTO viewDTO = null;
        if (responseData.success) {
            // Chỉ tạo DTO con nếu thành công
            viewDTO = mapToViewDTO(responseData);
        }
        
        viewModel.loggedInUser = viewDTO;    
	}

	private LoginSuccessViewDTO mapToViewDTO(AuthenticateUserResponseData data) {
		LoginSuccessViewDTO dto = new LoginSuccessViewDTO();
        dto.token = data.token;
        dto.userId = data.userId;
        dto.email = data.email;
        dto.role = String.valueOf(data.role); // Biên dịch Enum -> String
        return dto;
	}
	
	public AuthenticateUserViewModel getModel() {
        return this.viewModel;
    }
}
