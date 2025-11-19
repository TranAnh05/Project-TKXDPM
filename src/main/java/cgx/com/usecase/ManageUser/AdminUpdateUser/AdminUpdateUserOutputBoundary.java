package cgx.com.usecase.ManageUser.AdminUpdateUser;


/**
 * Output Port cho Use Case Admin Cập nhật Người dùng.
 * Presenter (Layer 2) sẽ implement interface này.
 *
 * (Tái sử dụng ViewUserProfileResponseData vì nó chứa
 * đầy đủ thông tin user đã cập nhật).
 */
public interface AdminUpdateUserOutputBoundary {
	void present(AdminUpdateUserResponseData responseData);
}
