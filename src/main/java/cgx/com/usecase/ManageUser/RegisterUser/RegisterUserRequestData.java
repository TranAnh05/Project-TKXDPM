package cgx.com.usecase.ManageUser.RegisterUser;

public class RegisterUserRequestData {
	public final String email;
    public final String password;
    public final String firstName;
    public final String lastName;
    
    // (Có thể thêm các trường khác cho các loại đăng ký khác, 
    // ví dụ: String googleToken)

    public RegisterUserRequestData(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
