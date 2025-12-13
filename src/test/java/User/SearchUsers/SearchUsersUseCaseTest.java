package User.SearchUsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.Interface_Common.PaginationData;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.SearchUsers.SearchUsersOutputBoundary;
import cgx.com.usecase.ManageUser.SearchUsers.SearchUsersRequestData;
import cgx.com.usecase.ManageUser.SearchUsers.SearchUsersResponseData;
import cgx.com.usecase.ManageUser.SearchUsers.SearchUsersUseCase;
import cgx.com.usecase.ManageUser.SearchUsers.UserSearchCriteria;

@ExtendWith(MockitoExtension.class)
public class SearchUsersUseCaseTest {

    @Mock
    private IAuthTokenValidator tokenValidator;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private SearchUsersOutputBoundary outputBoundary;

    private SearchUsersUseCase useCase;

    // Dữ liệu mẫu
    private final String adminToken = "valid_admin_token";
    private final String adminId = "admin-001";

    @BeforeEach
    void setUp() {
        useCase = new SearchUsersUseCase(tokenValidator, userRepository, outputBoundary);
    }

    // --- HELPER METHODS ---
    private void mockAdminAuth() {
        AuthPrincipal principal = new AuthPrincipal(adminId, "admin@test.com", UserRole.ADMIN);
        when(tokenValidator.validate(adminToken)).thenReturn(principal);
    }

    // Case: không phải admin
    @Test
    void testExecute_Fail_NotAdmin() {
        // Arrange
        SearchUsersRequestData request = new SearchUsersRequestData(adminToken, "abc", 1, 10);
        
        AuthPrincipal customerPrincipal = new AuthPrincipal("cust-001", "cust@test.com", UserRole.CUSTOMER);
        when(tokenValidator.validate(adminToken)).thenReturn(customerPrincipal);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<SearchUsersResponseData> captor = ArgumentCaptor.forClass(SearchUsersResponseData.class);
        verify(outputBoundary).present(captor.capture());
        
        assertFalse(captor.getValue().success);
        assertEquals("Không có quyền truy cập.", captor.getValue().message); 
    }
   
    // Lấy danh sách thành công - 30 users
    @Test
    void testExecute_Success_WithPagination() {
        // Arrange
        mockAdminAuth();
        
        // Input: Muốn xem Trang 2, mỗi trang 10 dòng
        int inputPageNumber = 2;
        int inputPageSize = 10;
        String searchTerm = "Nguyen";
        SearchUsersRequestData request = new SearchUsersRequestData(adminToken, searchTerm, inputPageNumber, inputPageSize);

        // Giả lập dữ liệu trả về từ DB (List user)
        List<UserData> mockUsers = new ArrayList<>();
        mockUsers.add(new UserData("u1", "n1@test.com", "", "Nguyen", "A", "0900", UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now(), Instant.now()));
        
        // Kỳ vọng: UseCase phải gọi Repo với pageIndex = 1 (vì pageNumber 2 - 1 = 1)
        when(userRepository.search(any(UserSearchCriteria.class), eq(1), eq(10)))
            .thenReturn(mockUsers);
        
        // Giả lập: Tổng cộng tìm thấy 25 người
        // Với pageSize = 10 -> Sẽ có 3 trang (10, 10, 5)
        when(userRepository.count(any(UserSearchCriteria.class))).thenReturn(25L);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<SearchUsersResponseData> captor = ArgumentCaptor.forClass(SearchUsersResponseData.class);
        verify(outputBoundary).present(captor.capture());
        SearchUsersResponseData response = captor.getValue();

        // 1. Kiểm tra trạng thái chung
        assertTrue(response.success);
        assertEquals("Tìm kiếm thành công.", response.message);
        assertEquals(mockUsers, response.users);

        // 2. Kiểm tra Dữ liệu Phân trang (PaginationData)
        PaginationData pagination = response.pagination;
        assertNotNull(pagination);
        assertEquals(25, pagination.totalCount); // Tổng số user
        assertEquals(2, pagination.currentPage); // Trang hiện tại
        assertEquals(10, pagination.pageSize);   // Kích thước trang
        assertEquals(3, pagination.totalPages);  // 25 chia 10 = 2.5 -> làm tròn lên là 3 trang. Đúng logic!
        
        // 3. Kiểm tra UserSearchCriteria được tạo đúng
        ArgumentCaptor<UserSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(UserSearchCriteria.class);
        verify(userRepository).search(criteriaCaptor.capture(), eq(1), eq(10));
        assertEquals("Nguyen", criteriaCaptor.getValue().getSearchTerm());
    }
   
    // Lấy danh sách thành công - danh sách trống
    @Test
    void testExecute_Success_EmptyResult() {
        // Arrange
        mockAdminAuth();
        SearchUsersRequestData request = new SearchUsersRequestData(adminToken, "UnknownName", 1, 10);

        when(userRepository.search(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(userRepository.count(any())).thenReturn(0L);

        // Act
        useCase.execute(request);

        // Assert
        ArgumentCaptor<SearchUsersResponseData> captor = ArgumentCaptor.forClass(SearchUsersResponseData.class);
        verify(outputBoundary).present(captor.capture());
        SearchUsersResponseData response = captor.getValue();

        assertTrue(response.success);
        assertTrue(response.users.isEmpty());
        assertEquals(0, response.pagination.totalCount);
        assertEquals(0, response.pagination.totalPages);
    }
}