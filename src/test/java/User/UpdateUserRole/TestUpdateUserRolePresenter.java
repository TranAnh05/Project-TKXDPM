package User.UpdateUserRole;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.ManageUser.UpdateUserRole.UpdateUserRolePresenter;
import adapters.ManageUser.UpdateUserRole.UpdateUserRoleViewModel;
import application.dtos.ManageUser.UserOutputData;
import application.dtos.ManageUser.UpdateUserRole.UpdateUserRoleOutputData;
import domain.entities.Role;

public class TestUpdateUserRolePresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        UpdateUserRoleViewModel viewModel = new UpdateUserRoleViewModel();
        UpdateUserRolePresenter presenter = new UpdateUserRolePresenter(viewModel);
        UpdateUserRoleOutputData output = new UpdateUserRoleOutputData();
        output.success = true;
        output.updatedUser = new UserOutputData();
        output.updatedUser.role = Role.ADMIN; // <-- Enum
        
        // 2. Act
        presenter.present(output);
        
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success);
        assertEquals("ADMIN", viewModel.updatedUser.role); // Enum -> String
    }
}
