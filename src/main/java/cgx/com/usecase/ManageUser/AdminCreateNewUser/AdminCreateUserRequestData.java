package cgx.com.usecase.ManageUser.AdminCreateNewUser;

public class AdminCreateUserRequestData {
public final String authToken;
    
    public final String email;
    public final String password;
    public final String firstName;
    public final String lastName;
    public final String phoneNumber;
    public final String role;   
    public final String status; 

    public AdminCreateUserRequestData(String authToken, String email, String password, String firstName, String lastName,String phoneNumber, String role, String status) {
        this.authToken = authToken;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
    }
}
