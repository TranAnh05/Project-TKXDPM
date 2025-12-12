package cgx.com.usecase.ManageUser.SearchUsers;

import java.util.List;

import cgx.com.Entities.User;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class SearchUsersUseCase implements SearchUsersInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final SearchUsersOutputBoundary outputBoundary;
    
    public SearchUsersUseCase(IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            SearchUsersOutputBoundary outputBoundary) {
		this.tokenValidator = tokenValidator;
		this.userRepository = userRepository;
		this.outputBoundary = outputBoundary;
	}
    
    
	@Override
	public void execute(SearchUsersRequestData input) {
		SearchUsersResponseData output = new SearchUsersResponseData();

        try {
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            User.validateIsAdmin(principal.role);
            
            UserSearchCriteria criteria = new UserSearchCriteria(input.searchTerm);

            // Lấy dữ liệu
            // CSDL thường dùng page index (0-based), UI dùng page number (1-based)
            int pageIndex = input.pageNumber - 1; 
            List<UserData> users = userRepository.search(criteria, pageIndex, input.pageSize);
            
            // Lấy tổng số lượng 
            long totalCount = userRepository.count(criteria);
            
            // Tạo DTO Phân trang
            PaginationData pagination = new PaginationData(totalCount, input.pageNumber, input.pageSize);

            output.success = true;
            output.message = "Tìm kiếm thành công.";
            output.users = users;
            output.pagination = pagination;

        } catch (SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

        outputBoundary.present(output);
	}
}
