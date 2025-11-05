package User.UnblockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import User.FakeUserRepository;
import application.dtos.ManageUser.UserData;
import application.dtos.ManageUser.UnblockUser.UnblockUserInputData;
import application.dtos.ManageUser.UnblockUser.UnblockUserOutputData;
import application.ports.out.ManageUser.UserRepository;
import application.usecases.ManageUser.UnblockUser.UnblockUserUsecase;
import domain.entities.Role;

public class TestUnblockUserUseCase {
	private UnblockUserUsecase useCase;
    private UserRepository userRepo;
    
    private UserData blockedUser;

    @BeforeEach
    public void setup() {
        userRepo = new FakeUserRepository();
        useCase = new UnblockUserUsecase(userRepo, null);
        
        // Dữ liệu mồi: Một user ĐÃ BỊ KHÓA
        UserData cust = new UserData(0, "cust@test.com", "hash456", "Customer", "addr2", Role.CUSTOMER, true); // isBlocked = true
        blockedUser = userRepo.save(cust); // ID: 1
    }
    
    @Test
    public void testExecute_SuccessCase() {
        // 1. Arrange: Mở khóa Customer (ID 1)
        UnblockUserInputData input = new UnblockUserInputData(blockedUser.id);
        
        // 2. Act
        useCase.execute(input);

        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        UnblockUserOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        assertEquals("Mở khóa tài khoản thành công!", output.message);
        assertFalse(output.updatedUser.isBlocked); // <-- Kiểm tra
        
        // Kiểm tra CSDL giả
        assertFalse(userRepo.findById(blockedUser.id).isBlocked);
    }
    
    @Test
    public void testExecute_Fail_UserNotFound() {
        // 1. Arrange: Mở khóa User ID 99
        UnblockUserInputData input = new UnblockUserInputData(99);
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        UnblockUserOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Không tìm thấy người dùng để mở khóa.", output.message);
    }
}
