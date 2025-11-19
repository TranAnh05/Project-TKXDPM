package User.SearchUsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Entities.UserRole;
import usecase.ManageUser.IAuthTokenValidator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;
import usecase.ManageUser.SearchUsers.SearchUsersByNameOrEmailUseCase;
import usecase.ManageUser.SearchUsers.SearchUsersOutputBoundary;
import usecase.ManageUser.SearchUsers.SearchUsersRequestData;
import usecase.ManageUser.SearchUsers.SearchUsersResponseData;
import usecase.ManageUser.SearchUsers.UserSearchCriteria;
import usecase.ManageUser.ViewUserProfile.AuthPrincipal;

@ExtendWith(MockitoExtension.class)
public class SearchUsersByNameOrEmailUseCaseTest {
	// 1. Mock các dependencies
    @Mock private IAuthTokenValidator mockTokenValidator;
    @Mock private IUserRepository mockUserRepository;
    @Mock private SearchUsersOutputBoundary mockOutputBoundary;

    // 2. Lớp cần test
    private SearchUsersByNameOrEmailUseCase useCase;

    // 3. Dữ liệu mẫu
    private SearchUsersRequestData requestData;
    private AuthPrincipal adminPrincipal;
    private UserData foundUser;
    
    @BeforeEach
    void setUp() {
        useCase = new SearchUsersByNameOrEmailUseCase(
            mockTokenValidator,
            mockUserRepository,
            mockOutputBoundary
        );

        // Yêu cầu tìm "john", trang 1, 10 mục
        requestData = new SearchUsersRequestData("Bearer admin.token", "john", 1, 10);
        
        // Giả lập Admin đã đăng nhập
        adminPrincipal = new AuthPrincipal("admin-123", "admin@example.com", UserRole.ADMIN);
        
        // Dữ liệu User tìm thấy
        foundUser = new UserData();
        foundUser.userId = "user-123";
        foundUser.email = "john.doe@example.com";
        foundUser.firstName = "John";
        foundUser.lastName = "Doe";
    }
    
    /**
     * Test kịch bản THÀNH CÔNG
     */
    @Test
    void test_execute_success() {
        // --- ARRANGE ---
        // 1. Giả lập Token Admin hợp lệ
        when(mockTokenValidator.validate("Bearer admin.token")).thenReturn(adminPrincipal);
        
        // 2. "Bắt" tiêu chí tìm kiếm
        ArgumentCaptor<UserSearchCriteria> criteriaCaptor = 
            ArgumentCaptor.forClass(UserSearchCriteria.class);
        
        // 3. Giả lập CSDL trả về 1 user
        when(mockUserRepository.search(criteriaCaptor.capture(), eq(0), eq(10)))
            .thenReturn(List.of(foundUser));
            
        // 4. Giả lập CSDL trả về tổng số là 1
        when(mockUserRepository.count(any(UserSearchCriteria.class))).thenReturn(1L);

        ArgumentCaptor<SearchUsersResponseData> responseCaptor =
            ArgumentCaptor.forClass(SearchUsersResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        // 1. Kiểm tra các bước
        verify(mockTokenValidator).validate("Bearer admin.token");
        verify(mockUserRepository).search(any(UserSearchCriteria.class), eq(0), eq(10));
        verify(mockUserRepository).count(any(UserSearchCriteria.class));
        
        // 2. Kiểm tra tiêu chí tìm kiếm đã được build đúng
        assertEquals("john", criteriaCaptor.getValue().getSearchTerm());
        
        // 3. Kiểm tra response
        verify(mockOutputBoundary).present(responseCaptor.capture());
        SearchUsersResponseData presentedResponse = responseCaptor.getValue();

        assertTrue(presentedResponse.success);
        assertEquals(1, presentedResponse.users.size(), "Phải trả về 1 user");
        assertEquals("john.doe@example.com", presentedResponse.users.get(0).email);
        
        // 4. Kiểm tra phân trang
        assertEquals(1, presentedResponse.pagination.totalCount);
        assertEquals(1, presentedResponse.pagination.totalPages);
        assertEquals(1, presentedResponse.pagination.currentPage);
    }
    
    /**
     * Test kịch bản THẤT BẠI: Không phải Admin
     */
    @Test
    void test_execute_failure_notAdmin() {
        // --- ARRANGE ---
        // 1. Giả lập Token hợp lệ nhưng là CUSTOMER
        AuthPrincipal customerPrincipal = new AuthPrincipal("user-123", "test@example.com", UserRole.CUSTOMER);
        when(mockTokenValidator.validate("Bearer customer.token")).thenReturn(customerPrincipal);
        
        ArgumentCaptor<SearchUsersResponseData> responseCaptor =
            ArgumentCaptor.forClass(SearchUsersResponseData.class);

        // --- ACT ---
        useCase.execute(new SearchUsersRequestData("Bearer customer.token", "john", 1, 10));
        
        // --- ASSERT ---
        verify(mockOutputBoundary).present(responseCaptor.capture());
        SearchUsersResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Không có quyền truy cập.", presentedResponse.message);
        
        // Quan trọng: Không bao giờ gọi CSDL
        verify(mockUserRepository, never()).search(any(), anyInt(), anyInt());
        verify(mockUserRepository, never()).count(any());
    }
    
    /**
     * Test kịch bản THẤT BẠI: Lỗi hệ thống (CSDL sập khi SEARCH)
     */
    @Test
    void test_execute_failure_systemErrorOnSearch() {
        // --- ARRANGE ---
        when(mockTokenValidator.validate("Bearer admin.token")).thenReturn(adminPrincipal);
        
        // 1. Giả lập CSDL sập khi SEARCH
        when(mockUserRepository.search(any(UserSearchCriteria.class), anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Database connection failed"));
            
        ArgumentCaptor<SearchUsersResponseData> responseCaptor =
            ArgumentCaptor.forClass(SearchUsersResponseData.class);

        // --- ACT ---
        useCase.execute(requestData);

        // --- ASSERT ---
        verify(mockUserRepository).search(any(UserSearchCriteria.class), anyInt(), anyInt());
        // Không gọi count() vì đã lỗi
        verify(mockUserRepository, never()).count(any(UserSearchCriteria.class));

        verify(mockOutputBoundary).present(responseCaptor.capture());
        SearchUsersResponseData presentedResponse = responseCaptor.getValue();

        assertFalse(presentedResponse.success);
        assertEquals("Đã xảy ra lỗi hệ thống không xác định.", presentedResponse.message);
    }
}
