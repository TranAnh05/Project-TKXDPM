package application.dtos.ManageUser;

import domain.entities.Role;

public class UserOutputData {
	public int id;
    public String email;
    public String fullName;
    public String address;
    public Role role;
    public boolean isBlocked;
    // <-- KHÔNG có passwordHash
}
