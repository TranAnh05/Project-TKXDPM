package User.SearchUsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;
import cgx.com.adapters.ManageUser.SearchUsers.SearchUsersPresenter;
import cgx.com.adapters.ManageUser.SearchUsers.SearchUsersViewModel;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.SearchUsers.PaginationData;
import cgx.com.usecase.ManageUser.SearchUsers.SearchUsersResponseData;

public class SearchUsersPresenterTest {
	private SearchUsersPresenter presenter;
    private SearchUsersViewModel viewModel;
    private SearchUsersResponseData responseData;

    @BeforeEach
    void setUp() {
        viewModel = new SearchUsersViewModel();
        presenter = new SearchUsersPresenter(viewModel);
        responseData = new SearchUsersResponseData();
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THÀNH CÔNG
     */
    @Test
    void test_present_successCase() {
        // --- ARRANGE ---
        // 1. Tạo UserData mẫu
        UserData user1 = new UserData("u1", "admin@e.com", "h", "Admin", "User", 
            null, UserRole.ADMIN, AccountStatus.ACTIVE, Instant.now(), Instant.now());
        UserData user2 = new UserData("u2", "cust@e.com", "h", "Customer", "User", 
            null, UserRole.CUSTOMER, AccountStatus.SUSPENDED, Instant.now(), Instant.now());

        // 2. Tạo PaginationData mẫu
        PaginationData pagination = new PaginationData(2, 1, 10); // 2 mục, trang 1/1, 10/trang

        // 3. Tạo ResponseData
        responseData.success = true;
        responseData.message = "Tìm kiếm thành công.";
        responseData.users = List.of(user1, user2);
        responseData.pagination = pagination;

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        SearchUsersViewModel resultVM = presenter.getModel();
        
        assertEquals("true", resultVM.success);
        assertEquals("Tìm kiếm thành công.", resultVM.message);
        
        // 1. Kiểm tra danh sách User đã được biên dịch
        assertNotNull(resultVM.users);
        assertEquals(2, resultVM.users.size());
        
        // Kiểm tra user 1 (Admin)
        assertEquals("u1", resultVM.users.get(0).id);
        assertEquals("Admin User", resultVM.users.get(0).fullName);
        assertEquals("ADMIN", resultVM.users.get(0).role);
        assertEquals("ACTIVE", resultVM.users.get(0).status);
        
        // Kiểm tra user 2 (Customer)
        assertEquals("u2", resultVM.users.get(1).id);
        assertEquals("Customer User", resultVM.users.get(1).fullName);
        assertEquals("CUSTOMER", resultVM.users.get(1).role);
        assertEquals("SUSPENDED", resultVM.users.get(1).status);

        // 2. Kiểm tra Phân trang đã được biên dịch (all strings)
        assertNotNull(resultVM.pagination);
        assertEquals("2", resultVM.pagination.totalCount);
        assertEquals("1", resultVM.pagination.currentPage);
        assertEquals("10", resultVM.pagination.pageSize);
        assertEquals("1", resultVM.pagination.totalPages);
    }
    
    /**
     * Test kịch bản Presenter nhận dữ liệu THẤT BẠI
     */
    @Test
    void test_present_failureCase() {
        // --- ARRANGE ---
        responseData.success = false;
        responseData.message = "Không có quyền truy cập.";

        // --- ACT ---
        presenter.present(responseData);

        // --- ASSERT ---
        SearchUsersViewModel resultVM = presenter.getModel();
        
        assertEquals("false", resultVM.success);
        assertEquals("Không có quyền truy cập.", resultVM.message);
        assertTrue(resultVM.users.isEmpty(), "Danh sách user nên rỗng khi thất bại");
        assertNull(resultVM.pagination, "Phân trang nên là null khi thất bại");
    }
}
