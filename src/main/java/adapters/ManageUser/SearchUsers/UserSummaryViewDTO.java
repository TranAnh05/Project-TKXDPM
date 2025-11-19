package adapters.ManageUser.SearchUsers;

public class UserSummaryViewDTO {
	public String id;
    public String email;
    public String fullName;
    public String role; // "CUSTOMER" hoặc "ADMIN"
    public String status; // "ACTIVE", "SUSPENDED", ...
}
