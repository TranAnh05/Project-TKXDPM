package User.BlockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.ManageUser.BlockUser.BlockUserPresenter;
import adapters.ManageUser.BlockUser.BlockUserViewModel;
import application.dtos.ManageUser.UserOutputData;
import application.dtos.ManageUser.BlockUser.BlockUserOutputData;

public class TestBlockUserPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        BlockUserViewModel viewModel = new BlockUserViewModel();
        BlockUserPresenter presenter = new BlockUserPresenter(viewModel);
        BlockUserOutputData output = new BlockUserOutputData();
        output.success = true;
        output.updatedUser = new UserOutputData();
        output.updatedUser.isBlocked = true; // <-- boolean
        
        // 2. Act
        presenter.present(output);
        
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", viewModel.success);
        assertEquals("true", viewModel.updatedUser.isBlocked); // boolean -> String
    }
}
