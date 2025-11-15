package User.ViewAllUsers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import Entities.UserRole;
import adapters.ManageUser.ViewAllUsers.ViewAllUsersPresenter;
import adapters.ManageUser.ViewAllUsers.ViewAllUsersViewModel;
import application.dtos.ManageUser.ViewAllUsers.ViewAllUsersOutputData;
import usecase.ManageUser.UserOutputData;

public class TestViewAllUsersPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        ViewAllUsersViewModel viewModel = new ViewAllUsersViewModel();
        ViewAllUsersPresenter presenter = new ViewAllUsersPresenter(viewModel);
        ViewAllUsersOutputData output = new ViewAllUsersOutputData();
        output.success = true;
        
        UserOutputData uData = new UserOutputData();
        uData.id = 1;
        uData.role = UserRole.ADMIN; // <-- Enum
        uData.isBlocked = true; // <-- boolean
        output.users = List.of(uData);
        
        // 2. Act
        presenter.present(output);
        
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success);
        assertEquals("1", viewModel.users.get(0).id);
        assertEquals("ADMIN", viewModel.users.get(0).role); // Enum -> String
        assertEquals("true", viewModel.users.get(0).isBlocked); // boolean -> String
    }
}
