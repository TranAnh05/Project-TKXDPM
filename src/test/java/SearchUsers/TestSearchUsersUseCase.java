package SearchUsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import User.FakeUserRepository;
import application.dtos.ManageUser.UserData;
import application.dtos.SearchUsers.SearchUsersInputData;
import application.dtos.SearchUsers.SearchUsersOutputData;
import application.ports.out.ManageUser.UserRepository;
import application.usecases.SearchUsers.SearchUsersUsecase;
import domain.entities.Role;

public class TestSearchUsersUseCase {
	private SearchUsersUsecase useCase;
    private UserRepository userRepo;
    
    @BeforeEach
    public void setup() {
        userRepo = new FakeUserRepository();
        useCase = new SearchUsersUsecase(userRepo, null);
        
        // Dữ liệu mồi
        userRepo.save(new UserData(0, "admin@test.com", "hash", "Admin", "Addr", Role.ADMIN, false));
        userRepo.save(new UserData(0, "user1@test.com", "hash", "User 1", "Addr", Role.CUSTOMER, false));
        userRepo.save(new UserData(0, "user2@gmail.com", "hash", "User 2", "Addr", Role.CUSTOMER, false));
    }
    
    @Test
    public void testExecute_SuccessCase_FoundMultiple() {
        // 1. Arrange: Tìm "@test.com"
        SearchUsersInputData input = new SearchUsersInputData("@test.com");
        
        // 2. Act
        useCase.execute(input);

        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        SearchUsersOutputData output = useCase.getOutputData();
        
        assertTrue(output.success);
        assertEquals(2, output.users.size()); // Tìm thấy 2 user
        assertEquals("admin@test.com", output.users.get(0).email);
        assertEquals("user1@test.com", output.users.get(1).email);
    }
    
    @Test
    public void testExecute_SuccessCase_NoData() {
        // 1. Arrange: Tìm "@yahoo.com" (không có)
        SearchUsersInputData input = new SearchUsersInputData("@yahoo.com");
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        SearchUsersOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        assertEquals("Không tìm thấy người dùng nào khớp với '@yahoo.com'.", output.message);
        assertEquals(0, output.users.size());
    }
}
