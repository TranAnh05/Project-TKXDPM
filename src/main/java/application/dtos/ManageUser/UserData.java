package application.dtos.ManageUser;

import domain.entities.Role;

public class UserData {
	public int id;
    public String email;
    public String passwordHash; // <-- Repo cần
    public String fullName;
    public String address;
    public Role role;
    public boolean isBlocked;
    
    public UserData() {}
    
    public UserData(int id, String email, String passwordHash, String fullName, String address, Role role, boolean isBlocked) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.address = address;
        this.role = role;
        this.isBlocked = isBlocked;
    }
}
