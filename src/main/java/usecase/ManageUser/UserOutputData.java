package usecase.ManageUser;

import Entities.UserRole;

public class UserOutputData {
	public int id;
    public String email;
    public String fullName;
    public String address;
    public UserRole role;
    public boolean isBlocked;
    // <-- KHÔNG có passwordHash
}
