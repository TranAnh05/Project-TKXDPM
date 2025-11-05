package application.ports.out.ManageUser;

import java.util.List;

import application.dtos.ManageUser.UserData;

public interface UserRepository {
	UserData findById(int id);
    UserData findByEmail(String email);
    List<UserData> findAll();
    UserData save(UserData userData);
    UserData update(UserData userData);
    // (Không có delete, chúng ta dùng update isBlocked)
    
    List<UserData> searchByEmail(String emailKeyword);
}
