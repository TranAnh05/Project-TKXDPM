package User.BlockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import User.FakeUserRepository;
import application.dtos.ManageUser.UserData;
import application.dtos.ManageUser.BlockUser.BlockUserInputData;
import application.dtos.ManageUser.BlockUser.BlockUserOutputData;
import application.ports.out.ManageUser.UserRepository;
import application.usecases.ManageUser.BlockUser.BlockUserUsecase;
import domain.entities.Role;

public class TestBlockUserUseCase {
	private BlockUserUsecase useCase;
    private UserRepository userRepo;
    
    private UserData adminUser;
    private UserData customerUser;
    
    @BeforeEach
    public void setup() {
        userRepo = new FakeUserRepository();
        useCase = new BlockUserUsecase(userRepo, null);
        
        // Dữ liệu mồi
        UserData admin = new UserData(0, "admin@test.com", "hash123", "Admin", "addr1", Role.ADMIN, false);
        UserData cust = new UserData(0, "cust@test.com", "hash456", "Customer", "addr2", Role.CUSTOMER, false);
        adminUser = userRepo.save(admin); // ID: 1
        customerUser = userRepo.save(cust); // ID: 2
    }
    
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange: Admin (ID 1) khóa Customer (ID 2)
        BlockUserInputData input = new BlockUserInputData(customerUser.id, adminUser.id);
        
        // 2. Act
        useCase.execute(input);

        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        BlockUserOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        assertEquals("Khóa tài khoản thành công!", output.message);
        assertTrue(output.updatedUser.isBlocked);
        
        // Kiểm tra CSDL giả
        assertTrue(userRepo.findById(customerUser.id).isBlocked);
    }
    
    @Test
    public void testExecute_Fail_AdminBlocksSelf() {
        // 1. Arrange: Admin (ID 1) tự khóa
        BlockUserInputData input = new BlockUserInputData(adminUser.id, adminUser.id);
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        BlockUserOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Không thể tự khóa tài khoản của chính mình.", output.message);
        assertFalse(userRepo.findById(adminUser.id).isBlocked); // (Không đổi)
    }
}
