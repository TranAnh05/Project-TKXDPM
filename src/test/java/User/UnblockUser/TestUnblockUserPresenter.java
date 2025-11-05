package User.UnblockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.ManageUser.UnblockUser.UnblockUserPresenter;
import adapters.ManageUser.UnblockUser.UnblockUserViewModel;
import application.dtos.ManageUser.UserOutputData;
import application.dtos.ManageUser.UnblockUser.UnblockUserOutputData;

public class TestUnblockUserPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        UnblockUserViewModel viewModel = new UnblockUserViewModel();
        UnblockUserPresenter presenter = new UnblockUserPresenter(viewModel);
        UnblockUserOutputData output = new UnblockUserOutputData();
        output.success = true;
        
        output.updatedUser = new UserOutputData();
        output.updatedUser.isBlocked = false; // <-- boolean
        
        // 2. Act
        presenter.present(output);
        
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success);
        assertEquals("false", viewModel.updatedUser.isBlocked); // boolean -> String
    }
}
