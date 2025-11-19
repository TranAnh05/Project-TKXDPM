package cgx.com.usecase.ManageUser.AdminCreateNewUser;

public class AdminCreateUserRequestData {
public final String authToken; // Token của Admin
    
    // Dữ liệu cho user mới
    public final String email;
    public final String password;
    public final String firstName;
    public final String lastName;
    public final String role;   // Dạng String (ví dụ: "ADMIN")
    public final String status; // Dạng String (ví dụ: "ACTIVE")

    public AdminCreateUserRequestData(String authToken, String email, String password, String firstName, String lastName, String role, String status) {
        this.authToken = authToken;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
    }
}
