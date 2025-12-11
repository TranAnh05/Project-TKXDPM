package cgx.com.usecase.ManageUser.ChangePassword;

public class ChangePasswordRequestData {
	public String authToken; // Token của người dùng
	    
    // Dữ liệu mật khẩu
    public String oldPassword;
    public String newPassword;
    
    public ChangePasswordRequestData(String authToken, String oldPassword, String newPassword) {
        this.authToken = authToken;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
