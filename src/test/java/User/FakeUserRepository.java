package User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import application.dtos.ManageUser.UserData;
import application.ports.out.ManageUser.UserRepository;

public class FakeUserRepository implements UserRepository{
	private Map<Integer, UserData> database = new HashMap<>();
    private int sequence = 0;
    
	@Override
	public UserData findById(int id) {
		return database.get(id);
	}

	@Override
	public UserData findByEmail(String email) {
		for (UserData d : database.values()) { if (d.email.equalsIgnoreCase(email)) return d; }
        return null;
	}

	@Override
	public List<UserData> findAll() {
		return new ArrayList<>(database.values());
	}

	@Override
	public UserData save(UserData userData) {
		sequence++;
        UserData savedData = new UserData(
            sequence, userData.email, userData.passwordHash,
            userData.fullName, userData.address, userData.role,
            userData.isBlocked
        );
        database.put(sequence, savedData);
        return savedData;
	}

	@Override
	public UserData update(UserData userData) {
		if (database.containsKey(userData.id)) {
            UserData updatedData = new UserData(
                userData.id, userData.email, userData.passwordHash,
                userData.fullName, userData.address, userData.role,
                userData.isBlocked
            );
            database.put(userData.id, updatedData);
            return updatedData;
        }
        return null;
	}

	@Override
	public List<UserData> searchByEmail(String emailKeyword) {
		List<UserData> results = new ArrayList<>();
        if (emailKeyword == null || emailKeyword.trim().isEmpty()) {
            return results; // Trả về list rỗng nếu không tìm gì
        }
        String lowerKeyword = emailKeyword.toLowerCase(Locale.ROOT);
        
        for (UserData data : database.values()) {
            if (data.email.toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
                results.add(data);
            }
        }
        
        return results; // Luôn trả về List, không trả về null
	}
}
