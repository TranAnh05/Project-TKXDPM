package adapters.ManageUser.RegisterUser;

import usecase.ManageUser.RegisterUser.RegisterUserOutputBoundary;
import usecase.ManageUser.RegisterUser.RegisterUserResponseData;

public class RegisterUserPresenter implements RegisterUserOutputBoundary {
	private RegisterUserViewModel viewModel;
	
	public RegisterUserPresenter(RegisterUserViewModel viewModel) {
	        this.viewModel = viewModel;
    }
	
	@Override
	public void present(RegisterUserResponseData responseData) {
		 // Map các trường chung
        viewModel.message = responseData.message;
        viewModel.success = String.valueOf(responseData.success); // Chuyển boolean -> String
        
        RegisteredUserViewDTO viewDTO = null;
        if (responseData.success) {
            // Chỉ tạo DTO con nếu thành công
            viewDTO = mapToViewDTO(responseData);
        }
        
        viewModel.newUser = viewDTO;
	}

    private RegisteredUserViewDTO mapToViewDTO(RegisterUserResponseData data) {
        RegisteredUserViewDTO dto = new RegisteredUserViewDTO();
        dto.id = data.createdUserId;
        dto.email = data.email;
        return dto;
    }
    
    public RegisterUserViewModel getModel() {
        return this.viewModel;
    }

}
