package User.UpdateUserRole;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import User.FakeUserRepository;
import application.dtos.ManageUser.UserData;
import application.dtos.ManageUser.UpdateUserRole.UpdateUserRoleInputData;
import application.dtos.ManageUser.UpdateUserRole.UpdateUserRoleOutputData;
import application.ports.out.ManageUser.UserRepository;
import application.usecases.ManageUser.UpdateUserRole.UpdateUserRoleUsecase;
import domain.entities.Role;

public class TestUpdateUserRoleUseCase {
	private UpdateUserRoleUsecase useCase;
    private UserRepository userRepo;
    
    private UserData adminUser;
    private UserData customerUser;
    
    @BeforeEach
    public void setup() {
        userRepo = new FakeUserRepository();
        useCase = new UpdateUserRoleUsecase(userRepo, null);
        
        // Dữ liệu mồi
        UserData admin = new UserData(0, "admin@test.com", "hash123", "Admin", "addr1", Role.ADMIN, false);
        UserData cust = new UserData(0, "cust@test.com", "hash456", "Customer", "addr2", Role.CUSTOMER, false);
        adminUser = userRepo.save(admin); // ID: 1
        customerUser = userRepo.save(cust); // ID: 2
    }
    
    @Test
    public void testExecute_SuccessCase_PromoteToAdmin() {
        // 1. Arrange: Admin (ID 1) thăng cấp Customer (ID 2) lên "ADMIN"
        UpdateUserRoleInputData input = new UpdateUserRoleInputData(
            customerUser.id, "ADMIN", adminUser.id
        );
        
        // 2. Act
        useCase.execute(input);

        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        UpdateUserRoleOutputData output = useCase.getOutputData();
        assertTrue(output.success);
        assertEquals(Role.ADMIN, output.updatedUser.role);
        assertEquals(customerUser.id, output.updatedUser.id);
        
        // Kiểm tra CSDL giả
        assertEquals(Role.ADMIN, userRepo.findById(customerUser.id).role);
    }
    
    @Test
    public void testExecute_Fail_AdminUpdatesSelf() {
        // 1. Arrange: Admin (ID 1) tự sửa
        UpdateUserRoleInputData input = new UpdateUserRoleInputData(adminUser.id, "CUSTOMER", adminUser.id);
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert (KIỂM TRA OUTPUTDATA - Tầng 3)
        UpdateUserRoleOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertEquals("Không thể tự thay đổi vai trò của chính mình.", output.message);
        assertEquals(Role.ADMIN, userRepo.findById(adminUser.id).role); // (Không đổi)
    }
    
    @Test
    public void testExecute_Fail_Validation_InvalidRoleString() {
        // 1. Arrange: Vai trò "MOD" không hợp lệ
        UpdateUserRoleInputData input = new UpdateUserRoleInputData(customerUser.id, "MOD", adminUser.id);
        
        // 2. Act
        useCase.execute(input);
        
        // 3. Assert
        UpdateUserRoleOutputData output = useCase.getOutputData();
        assertFalse(output.success);
        assertTrue(output.message.contains("không hợp lệ")); // (Kiểm tra Tầng 4 ném lỗi)
    }
}
