package cgx.com.adapters.ManageUser.AdminUpdateUser;

import cgx.com.adapters.ManageUser.AdminViewUserDetails.AdminManagedUserViewDTO;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserOutputBoundary;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserResponseData;

public class AdminUpdateUserPresenter implements AdminUpdateUserOutputBoundary{
	private AdminUpdateUserViewModel viewModel;

    public AdminUpdateUserPresenter(AdminUpdateUserViewModel viewModel) {
        this.viewModel = viewModel;
    }
	
	@Override
	public void present(AdminUpdateUserResponseData responseData) {
		viewModel.success = String.valueOf(responseData.success);
        viewModel.message = responseData.message;

        // 2. Xử lý DTO lồng nhau
        AdminManagedUserViewDTO viewDTO = null;
        if (responseData.success) {
            // Chỉ tạo DTO con nếu thành công
            viewDTO = mapToViewDTO(responseData);
        }
        
        viewModel.updatedUser = viewDTO;
	}

	private AdminManagedUserViewDTO mapToViewDTO(AdminUpdateUserResponseData data) {
		AdminManagedUserViewDTO dto = new AdminManagedUserViewDTO(); // <-- ĐÃ THAY ĐỔI
        dto.id = data.userId;
        dto.email = data.email;
        dto.firstName = data.firstName;
        dto.lastName = data.lastName;
        // Đảm bảo không bao giờ trả về null cho UI
        dto.phoneNumber = (data.phoneNumber == null) ? "" : data.phoneNumber;
        
        // Các trường này chỉ Admin mới thấy
        dto.role = String.valueOf(data.role); // Biên dịch Enum -> String
        dto.status = String.valueOf(data.status); // Biên dịch Enum -> String

        return dto;
	}
	
	public AdminUpdateUserViewModel getModel() {
        return this.viewModel;
    }

}
