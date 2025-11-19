package cgx.com.usecase.ManageUser.ChangePassword;

public class ChangePasswordRequestData {
	public final String authToken; // Token của người dùng
	    
    // Dữ liệu mật khẩu
    public final String oldPassword;
    public final String newPassword;

    public ChangePasswordRequestData(String authToken, String oldPassword, String newPassword) {
        this.authToken = authToken;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
