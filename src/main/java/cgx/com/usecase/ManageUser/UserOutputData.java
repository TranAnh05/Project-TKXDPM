package cgx.com.usecase.ManageUser;

import cgx.com.Entities.UserRole;

public class UserOutputData {
	public int id;
    public String email;
    public String fullName;
    public String address;
    public UserRole role;
    public boolean isBlocked;
    // <-- KHÔNG có passwordHash
}
