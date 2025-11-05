package SearchUsers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.SearchUsers.SearchUsersPresenter;
import adapters.SearchUsers.SearchUsersViewModel;
import application.dtos.SearchUsers.SearchUsersOutputData;

public class TestSearchUsersPresenter {
	@Test
    public void testPresent_Conversion() {
        // 1. Arrange
        SearchUsersViewModel viewModel = new SearchUsersViewModel();
        SearchUsersPresenter presenter = new SearchUsersPresenter(viewModel);
        SearchUsersOutputData output = new SearchUsersOutputData();
        output.success = false;
        output.message = "Lỗi";
        
        // 2. Act
        presenter.present(output);
        
        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("false", viewModel.success);
        assertEquals("Lỗi", viewModel.message);
    }
}
