package User.ViewAllUsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Entities.UserRole;
import User.FakeUserRepository;
import adapters.ManageUser.ViewAllUsers.ViewAllUsersPresenter;
import adapters.ManageUser.ViewAllUsers.ViewAllUsersViewModel;
import application.dtos.ManageUser.ViewAllUsers.ViewAllUsersOutputData;
import application.ports.out.ManageUser.UserRepository;
import application.usecases.ManageUser.ViewAllUsers.ViewAllUsersUsecase;
import usecase.ManageUser.UserData;
import usecase.ManageUser.UserOutputData;

public class TestViewAllUsersUseCase {
	private ViewAllUsersUsecase useCase;
    private UserRepository userRepo;
    private ViewAllUsersViewModel viewModel;
    private ViewAllUsersPresenter presenter;
    
    @BeforeEach
    public void setup() {
        userRepo = new FakeUserRepository();
        viewModel = new ViewAllUsersViewModel();
        presenter = new ViewAllUsersPresenter(viewModel);
        useCase = new ViewAllUsersUsecase(userRepo, presenter);
    }
    
    @Test
    public void testExecute_SuccessCase_WithData() {
        // 1. Arrange: Tạo 1 UserData (có passwordHash)
        UserData admin = new UserData(0, "admin@test.com", "hash123", "Admin", "addr1", UserRole.ADMIN, false);
        userRepo.save(admin);

        // 2. Act
        useCase.execute();

        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        ViewAllUsersOutputData output = useCase.getOuputData();
        
        assertTrue(output.success);
        assertNull(output.message);
        assertEquals(1, output.users.size());
        
        // 4. KIỂM TRA BẢO MẬT (Quan trọng)
        UserOutputData result = output.users.get(0);
        assertEquals("admin@test.com", result.email);
        assertEquals(UserRole.ADMIN, result.role);
    }

    @Test
    public void testExecute_SuccessCase_NoData() {
        useCase.execute();
        ViewAllUsersOutputData output = useCase.getOuputData();
        assertTrue(output.success);
        assertEquals("Không tìm thấy người dùng nào.", output.message);
        assertEquals(0, output.users.size());
    }
}
